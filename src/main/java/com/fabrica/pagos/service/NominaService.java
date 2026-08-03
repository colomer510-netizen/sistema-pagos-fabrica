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
    private final ContabilidadService contabilidadService;

    public NominaService(EmpleadoRepository empleadoRepository,
                         AsistenciaRepository asistenciaRepository,
                         NominaRepository nominaRepository,
                         ReciboRepository reciboRepository,
                         DeduccionConfigRepository deduccionConfigRepository,
                         ContabilidadService contabilidadService) {
        this.empleadoRepository = empleadoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.nominaRepository = nominaRepository;
        this.reciboRepository = reciboRepository;
        this.deduccionConfigRepository = deduccionConfigRepository;
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
            Integer horas = asistenciaRepository.sumHorasPorPeriodo(empleado, inicio, fin);
            int horasTrabajadas = horas == null ? 0 : horas;
            if (horasTrabajadas == 0) {
                continue;
            }

            int diasLaborables = diasLaborables(inicio, fin);
            int horasEsperadas = diasLaborables * empleado.getHorasJornada();
            int horasExtras = Math.max(0, horasTrabajadas - horasEsperadas);
            int horasNormales = horasTrabajadas - horasExtras;

            BigDecimal bruto = contabilidadService.calcularSalarioBruto(empleado, horasNormales, horasExtras);
            BigDecimal inss = contabilidadService.calcularInssLaboral(bruto);
            BigDecimal otrasDeducciones = calcularOtrasDeducciones(bruto, deduccionesActivas);
            BigDecimal neto = contabilidadService.calcularSalarioNeto(bruto, inss, otrasDeducciones);

            Recibo recibo = new Recibo();
            recibo.setNomina(nomina);
            recibo.setEmpleado(empleado);
            recibo.setHorasNormales(horasNormales);
            recibo.setHorasExtras(horasExtras);
            recibo.setSalarioBruto(bruto);
            recibo.setDescuentoInss(inss);
            recibo.setSalarioNeto(neto);
            reciboRepository.save(recibo);

            totalEmpleados++;
            totalHoras += horasTrabajadas;
            totalPagar = totalPagar.add(neto);
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

    private BigDecimal calcularOtrasDeducciones(BigDecimal bruto, List<DeduccionConfig> deducciones) {
        BigDecimal total = BigDecimal.ZERO;
        for (DeduccionConfig d : deducciones) {
            total = total.add(bruto.multiply(d.getPorcentaje()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
        return total;
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

    public List<Recibo> getRecibosPorEmpleado(Long empleadoId) {
        return reciboRepository.findByEmpleadoIdOrderByNominaFechaGeneracionDesc(empleadoId);
    }

    public void eliminarNomina(Long id) {
        List<Recibo> recibos = reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(id);
        reciboRepository.deleteAll(recibos);
        nominaRepository.deleteById(id);
    }
}
