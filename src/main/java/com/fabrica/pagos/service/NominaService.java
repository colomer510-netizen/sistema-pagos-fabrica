package com.fabrica.pagos.service;

import com.fabrica.pagos.model.*;
import com.fabrica.pagos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NominaService {

    private final EmpleadoRepository empleadoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final NominaRepository nominaRepository;
    private final ReciboRepository reciboRepository;
    private final DeduccionConfigRepository deduccionConfigRepository;
    private final DeduccionAplicadaRepository deduccionAplicadaRepository;
    private final PrestamoRepository prestamoRepository;
    private final ContabilidadService contabilidadService;

    public NominaService(EmpleadoRepository empleadoRepository,
                         AsistenciaRepository asistenciaRepository,
                         NominaRepository nominaRepository,
                         ReciboRepository reciboRepository,
                         DeduccionConfigRepository deduccionConfigRepository,
                         DeduccionAplicadaRepository deduccionAplicadaRepository,
                         PrestamoRepository prestamoRepository,
                         ContabilidadService contabilidadService) {
        this.empleadoRepository = empleadoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.nominaRepository = nominaRepository;
        this.reciboRepository = reciboRepository;
        this.deduccionConfigRepository = deduccionConfigRepository;
        this.deduccionAplicadaRepository = deduccionAplicadaRepository;
        this.prestamoRepository = prestamoRepository;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Genera la nómina del periodo: suma asistencia por empleado,
     * calcula horas normales/extras y genera recibos individuales.
     */
    @Transactional
    public Nomina generarNomina(LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la de fin");
        }
        if (nominaRepository.existsByPeriodoInicioAndPeriodoFin(inicio, fin)) {
            throw new IllegalArgumentException("Ya existe una nómina para este periodo");
        }

        List<Empleado> empleados = empleadoRepository.findByActivoTrueOrderByCodigoAsc();
        if (empleados.isEmpty()) {
            throw new IllegalArgumentException("No hay empleados activos para generar la nómina");
        }

        String numero = generarNumeroNomina();
        Nomina nomina = new Nomina();
        nomina.setNumero(numero);
        nomina.setPeriodoInicio(inicio);
        nomina.setPeriodoFin(fin);
        nomina.setTotalEmpleados(0);
        nomina.setTotalHoras(BigDecimal.ZERO);
        nomina.setTotalPagar(BigDecimal.ZERO);
        nomina = nominaRepository.save(nomina);

        List<DeduccionConfig> deduccionesActivas = deduccionConfigRepository.findByActivaTrueOrderByNombreAsc();

        int totalEmpleados = 0;
        int totalHoras = 0;
        BigDecimal totalPagar = BigDecimal.ZERO;

        for (Empleado empleado : empleados) {
            ResultadoCalculo res = calcularEmpleado(empleado, deduccionesActivas, inicio, fin);
            if (res == null) {
                continue;
            }

            Recibo recibo = new Recibo();
            recibo.setNomina(nomina);
            recibo.setEmpleado(empleado);
            recibo.setHorasNormales(res.horasNormales);
            recibo.setHorasExtras(res.horasExtras);
            recibo.setSalarioBruto(res.bruto);
            recibo.setDescuentoInss(res.inss);
            recibo.setTotalDeducciones(res.totalDeducciones);
            recibo.setSalarioNeto(res.neto);
            recibo = reciboRepository.save(recibo);

            for (DeduccionConfig d : res.aplicadas) {
                DeduccionAplicada item = new DeduccionAplicada();
                item.setRecibo(recibo);
                item.setNombre(d.getNombre());
                item.setMonto(d.getMontoCalculado());
                deduccionAplicadaRepository.save(item);
            }

            if (res.prestamoActivo != null && res.cuotaPrestamo.compareTo(BigDecimal.ZERO) > 0) {
                DeduccionAplicada item = new DeduccionAplicada();
                item.setRecibo(recibo);
                item.setNombre("Préstamo");
                item.setMonto(res.cuotaPrestamo);
                deduccionAplicadaRepository.save(item);

                BigDecimal nuevoSaldo = res.prestamoActivo.getSaldo().subtract(res.cuotaPrestamo);
                res.prestamoActivo.setSaldo(nuevoSaldo);
                if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
                    res.prestamoActivo.setActivo(false);
                }
                prestamoRepository.save(res.prestamoActivo);
            }

            totalEmpleados++;
            totalHoras += res.horasTrabajadas;
            totalPagar = totalPagar.add(res.neto);
        }

        if (totalEmpleados == 0) {
            nominaRepository.delete(nomina);
            throw new IllegalArgumentException("No hay asistencia registrada en este periodo para generar la nómina");
        }

        nomina.setTotalEmpleados(totalEmpleados);
        nomina.setTotalHoras(BigDecimal.valueOf(totalHoras));
        nomina.setTotalPagar(totalPagar.setScale(2, RoundingMode.HALF_UP));
        return nominaRepository.save(nomina);
    }

    /**
     * Vista previa de la nómina: calcula lo que se generaría sin persistir nada.
     */
    public List<NominaPreviewItem> previewNomina(LocalDate inicio, LocalDate fin) {
        List<Empleado> empleados = empleadoRepository.findByActivoTrueOrderByCodigoAsc();
        List<DeduccionConfig> deduccionesActivas = deduccionConfigRepository.findByActivaTrueOrderByNombreAsc();
        List<NominaPreviewItem> items = new ArrayList<>();

        for (Empleado empleado : empleados) {
            ResultadoCalculo res = calcularEmpleado(empleado, deduccionesActivas, inicio, fin);
            if (res == null) {
                continue;
            }
            NominaPreviewItem item = new NominaPreviewItem();
            item.setEmpleadoId(empleado.getId());
            item.setCodigo(empleado.getCodigo());
            item.setNombreCompleto(empleado.getNombreCompleto());
            item.setCargo(empleado.getCargo());
            item.setDepartamento(empleado.getDepartamento());
            item.setHorasNormales(res.horasNormales);
            item.setHorasExtras(res.horasExtras);
            item.setSalarioBruto(res.bruto);
            item.setInss(res.inss);
            item.setDeducciones(res.aplicadas.stream()
                    .map(d -> new NominaPreviewItem.DeduccionPreview(d.getNombre(), d.getMontoCalculado()))
                    .collect(java.util.stream.Collectors.toList()));
            item.setCuotaPrestamo(res.cuotaPrestamo);
            item.setTotalDeducciones(res.totalDeducciones);
            item.setSalarioNeto(res.neto);
            items.add(item);
        }
        return items;
    }

    private ResultadoCalculo calcularEmpleado(Empleado empleado, List<DeduccionConfig> deduccionesActivas,
                                              LocalDate inicio, LocalDate fin) {
        Integer horas = asistenciaRepository.sumHorasPorPeriodo(empleado, inicio, fin);
        int horasTrabajadas = horas == null ? 0 : horas;
        if (horasTrabajadas == 0) {
            return null;
        }

        int diasLaborables = diasLaborables(inicio, fin);
        int horasEsperadas = diasLaborables * empleado.getHorasJornada();
        int horasExtras = Math.max(0, horasTrabajadas - horasEsperadas);
        int horasNormales = horasTrabajadas - horasExtras;

        BigDecimal bruto = contabilidadService.calcularSalarioBruto(empleado, horasNormales, horasExtras);
        BigDecimal inss = contabilidadService.calcularInssLaboral(bruto);
        List<DeduccionConfig> aplicadas = calcularDeduccionesConfiguradas(bruto, deduccionesActivas);
        BigDecimal otrasDeducciones = aplicadas.stream()
                .map(DeduccionConfig::getMontoCalculado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Prestamo prestamoActivo = prestamoRepository.findByEmpleadoIdAndActivoTrueOrderByFechaDesc(empleado.getId())
                .stream().findFirst().orElse(null);
        BigDecimal cuotaPrestamo = BigDecimal.ZERO;
        if (prestamoActivo != null) {
            cuotaPrestamo = prestamoActivo.getCuota().min(prestamoActivo.getSaldo());
        }

        BigDecimal totalDeducciones = inss.add(otrasDeducciones).add(cuotaPrestamo);
        BigDecimal neto = contabilidadService.calcularSalarioNeto(bruto, inss, otrasDeducciones.add(cuotaPrestamo));

        ResultadoCalculo res = new ResultadoCalculo();
        res.horasTrabajadas = horasTrabajadas;
        res.horasNormales = horasNormales;
        res.horasExtras = horasExtras;
        res.bruto = bruto;
        res.inss = inss;
        res.aplicadas = aplicadas;
        res.prestamoActivo = prestamoActivo;
        res.cuotaPrestamo = cuotaPrestamo;
        res.totalDeducciones = totalDeducciones;
        res.neto = neto;
        return res;
    }

    private static class ResultadoCalculo {
        int horasTrabajadas;
        int horasNormales;
        int horasExtras;
        BigDecimal bruto;
        BigDecimal inss;
        List<DeduccionConfig> aplicadas;
        Prestamo prestamoActivo;
        BigDecimal cuotaPrestamo;
        BigDecimal totalDeducciones;
        BigDecimal neto;
    }

    private List<DeduccionConfig> calcularDeduccionesConfiguradas(BigDecimal bruto, List<DeduccionConfig> deducciones) {
        List<DeduccionConfig> resultado = new ArrayList<>();
        for (DeduccionConfig d : deducciones) {
            BigDecimal monto;
            if ("IR".equalsIgnoreCase(d.getTipoCalculo())) {
                monto = contabilidadService.calcularImpuestoRenta(bruto);
            } else if (d.getPorcentaje() != null) {
                monto = bruto.multiply(d.getPorcentaje()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                monto = BigDecimal.ZERO;
            }
            d.setMontoCalculado(monto);
            resultado.add(d);
        }
        return resultado;
    }

    private int diasLaborables(LocalDate inicio, LocalDate fin) {
        int dias = 0;
        LocalDate d = inicio;
        while (!d.isAfter(fin)) {
            if (d.getDayOfWeek().getValue() < 6) {
                dias++;
            }
            d = d.plusDays(1);
        }
        return dias;
    }

    private String generarNumeroNomina() {
        String base = "NOM-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        int secuencia = 1;
        var ultima = nominaRepository.findTopByOrderByNumeroDesc().orElse(null);
        if (ultima != null && ultima.getNumero().startsWith(base)) {
            secuencia = Integer.parseInt(ultima.getNumero().substring(base.length() + 1)) + 1;
        }
        return base + "-" + String.format("%03d", secuencia);
    }

    public List<Recibo> getRecibos(Nomina nomina) {
        return reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(nomina.getId());
    }

    public List<DeduccionAplicada> getDeduccionesDelRecibo(Long reciboId) {
        return deduccionAplicadaRepository.findByReciboIdOrderByIdAsc(reciboId);
    }

    public java.util.Map<Long, List<DeduccionAplicada>> getDeduccionesPorRecibo(Nomina nomina) {
        java.util.Map<Long, List<DeduccionAplicada>> mapa = new java.util.HashMap<>();
        for (Recibo r : getRecibos(nomina)) {
            mapa.put(r.getId(), getDeduccionesDelRecibo(r.getId()));
        }
        return mapa;
    }

    public List<Recibo> getRecibosPorEmpleado(Long empleadoId) {
        return reciboRepository.findByEmpleadoIdOrderByNominaFechaGeneracionDesc(empleadoId);
    }

    public void eliminarNomina(Long id) {
        List<Recibo> recibos = reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(id);
        reciboRepository.deleteAll(recibos);
        nominaRepository.deleteById(id);
    }
}
