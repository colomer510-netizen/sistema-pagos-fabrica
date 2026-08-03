package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Liquidacion;
import com.fabrica.pagos.repository.PermisoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LiquidacionService {

    private final PermisoRepository permisoRepository;

    @Value("${app.rrhh.dias-vacaciones-por-anyo:15}")
    private int diasVacacionesPorAnyo;

    @Value("${app.rrhh.dias-indemnizacion-por-anyo:30}")
    private int diasIndemnizacionPorAnyo;

    public LiquidacionService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    public Liquidacion calcular(Empleado empleado, LocalDate fechaSalida, String motivo,
                                BigDecimal otros, String observacion) {
        Liquidacion l = new Liquidacion();
        l.setEmpleado(empleado);
        l.setFechaSalida(fechaSalida);
        l.setMotivo(motivo);
        l.setOtros(otros == null ? BigDecimal.ZERO : otros);
        l.setObservacion(observacion);

        int horasJornada = empleado.getHorasJornada() == null ? 8 : empleado.getHorasJornada();
        BigDecimal salarioDiario = empleado.getTarifaHora().multiply(BigDecimal.valueOf(horasJornada));

        double aniosServicio = Math.max(0,
                ChronoUnit.DAYS.between(empleado.getFechaContratacion(), fechaSalida) / 365.25);

        int vacacionesGeneradas = (int) Math.round(aniosServicio * diasVacacionesPorAnyo);
        int vacacionesUsadas = permisoRepository
                .findByEmpleadoIdAndTipoAndEstadoAndFechaFinLessThanEqual(
                        empleado.getId(), "VACACIONES", "APROBADO", fechaSalida)
                .stream()
                .mapToInt(p -> p.getDias() == null ? 0 : p.getDias())
                .sum();
        int pendientes = Math.max(0, vacacionesGeneradas - vacacionesUsadas);
        l.setDiasVacacionesPendientes(pendientes);
        l.setMontoVacaciones(salarioDiario.multiply(BigDecimal.valueOf(pendientes))
                .setScale(2, RoundingMode.HALF_UP));

        if ("DESPIDO".equalsIgnoreCase(motivo)) {
            BigDecimal indemnizacion = salarioDiario
                    .multiply(BigDecimal.valueOf(diasIndemnizacionPorAnyo))
                    .multiply(BigDecimal.valueOf(aniosServicio))
                    .setScale(2, RoundingMode.HALF_UP);
            l.setIndemnizacion(indemnizacion);
        } else {
            l.setIndemnizacion(BigDecimal.ZERO);
        }

        l.setTotal(l.getMontoVacaciones().add(l.getIndemnizacion()).add(l.getOtros())
                .setScale(2, RoundingMode.HALF_UP));
        return l;
    }
}
