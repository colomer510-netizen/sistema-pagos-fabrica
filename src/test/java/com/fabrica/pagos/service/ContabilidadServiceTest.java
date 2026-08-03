package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empleado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContabilidadServiceTest {

    private final ContabilidadService service =
            new ContabilidadService(new BigDecimal("7.0"), new BigDecimal("22.5"), new BigDecimal("2.0"));

    private Empleado empleado() {
        Empleado e = new Empleado();
        e.setTarifaHora(new BigDecimal("100.00"));
        e.setHorasJornada(8);
        e.setFechaContratacion(LocalDate.of(2020, 1, 1));
        return e;
    }

    @Test
    void calcularSalarioBruto_sumaNormalesYExtras_sinMultiplicador() {
        assertEquals(new BigDecimal("1000.00"), service.calcularSalarioBruto(empleado(), 8, 2));
    }

    @Test
    void calcularSalarioBruto_sinHoras_esCero() {
        assertEquals(new BigDecimal("0.00"), service.calcularSalarioBruto(empleado(), 0, 0));
    }

    @Test
    void calcularInssLaboral_aplica7Porciento() {
        assertEquals(new BigDecimal("70.00"), service.calcularInssLaboral(new BigDecimal("1000.00")));
    }

    @Test
    void calcularInssPatronal_aplica22Punto5Porciento() {
        assertEquals(new BigDecimal("225.00"), service.calcularInssPatronal(new BigDecimal("1000.00")));
    }

    @Test
    void calcularImpuestoRenta_aplica2Porciento() {
        assertEquals(new BigDecimal("20.00"), service.calcularImpuestoRenta(new BigDecimal("1000.00")));
    }

    @Test
    void calcularSalarioNeto_restaDeducciones() {
        assertEquals(new BigDecimal("910.00"),
                service.calcularSalarioNeto(new BigDecimal("1000.00"),
                        new BigDecimal("70.00"), new BigDecimal("20.00")));
    }

    @Test
    void calcularVacaciones_15DiasPorCadaSeisMeses() {
        assertEquals(new BigDecimal("12000.00"), service.calcularVacaciones(empleado(), 6));
    }

    @Test
    void calcularAguinaldo_proporcionalAlMes() {
        assertEquals(new BigDecimal("12000.00"), service.calcularAguinaldo(empleado(), 6));
    }

    @Test
    void calcularIndemnizacion_unMesPorAnio() {
        assertEquals(new BigDecimal("72000.00"), service.calcularIndemnizacion(empleado(), 3));
    }

    @Test
    void mesesDesdeContratacion_nuncaNegativo() {
        assertTrue(service.mesesDesdeContratacion(empleado()) >= 0);
    }
}
