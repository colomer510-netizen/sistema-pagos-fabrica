package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empleado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ContabilidadService {

    private final BigDecimal inssLaboral;
    private final BigDecimal inssPatronal;
    private final BigDecimal porcentajeIr;

    public ContabilidadService(
            @Value("${app.contabilidad.deduccion-inss:7.0}") BigDecimal inssLaboral,
            @Value("${app.contabilidad.deduccion-inss-patronal:22.5}") BigDecimal inssPatronal,
            @Value("${app.contabilidad.deduccion-ir:2.0}") BigDecimal porcentajeIr) {
        this.inssLaboral = inssLaboral;
        this.inssPatronal = inssPatronal;
        this.porcentajeIr = porcentajeIr;
    }

    public BigDecimal getInssLaboral() { return inssLaboral; }
    public BigDecimal getInssPatronal() { return inssPatronal; }

    /**
     * Calcula el pago bruto por horas de un empleado en un periodo.
     * horasExtras = horas totales - horas de jornada esperadas en el periodo.
     */
    public BigDecimal calcularSalarioBruto(Empleado empleado, int horasNormales, int horasExtras) {
        BigDecimal tarifa = empleado.getTarifaHora();
        BigDecimal pagoNormal = tarifa.multiply(BigDecimal.valueOf(horasNormales));
        BigDecimal pagoExtra = tarifa.multiply(BigDecimal.valueOf(horasExtras));
        return pagoNormal.add(pagoExtra).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la deducción de INSS laboral (porcentaje sobre el salario bruto).
     */
    public BigDecimal calcularInssLaboral(BigDecimal salarioBruto) {
        return salarioBruto.multiply(inssLaboral).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Impuesto sobre la renta simplificado (configurable por umbrales).
     * Solo aplica sobre el excedente del salario bruto sobre el monto exento.
     */
    public BigDecimal calcularImpuestoRenta(BigDecimal salarioBruto) {
        return salarioBruto.multiply(porcentajeIr)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    /**
     * Calcula la cuota patronal de INSS (costo del empleador, se reporta en contabilidad).
     */
    public BigDecimal calcularInssPatronal(BigDecimal salarioBruto) {
        return salarioBruto.multiply(inssPatronal).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Salario neto = bruto - deducciones laborales.
     */
    public BigDecimal calcularSalarioNeto(BigDecimal salarioBruto, BigDecimal inssLaboral, BigDecimal otrasDeducciones) {
        return salarioBruto.subtract(inssLaboral).subtract(otrasDeducciones).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Vacaciones anuales: 15 días de salario por cada 6 meses trabajados (genérico latinoamericano).
     */
    public BigDecimal calcularVacaciones(Empleado empleado, int mesesTrabajados) {
        BigDecimal salarioDiario = empleado.getTarifaHora()
                .multiply(BigDecimal.valueOf(empleado.getHorasJornada()));
        BigDecimal periodos = BigDecimal.valueOf(mesesTrabajados).divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
        return salarioDiario.multiply(BigDecimal.valueOf(15)).multiply(periodos).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Aguinaldo anual: equivalente a un mes de salario (genérico).
     */
    public BigDecimal calcularAguinaldo(Empleado empleado, int mesesTrabajados) {
        BigDecimal salarioMensual = empleado.getTarifaHora()
                .multiply(BigDecimal.valueOf(empleado.getHorasJornada()))
                .multiply(BigDecimal.valueOf(30));
        BigDecimal proporcion = BigDecimal.valueOf(mesesTrabajados).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return salarioMensual.multiply(proporcion).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Indemnización: un mes de salario por cada año trabajado.
     */
    public BigDecimal calcularIndemnizacion(Empleado empleado, int aniosTrabajados) {
        BigDecimal salarioMensual = empleado.getTarifaHora()
                .multiply(BigDecimal.valueOf(empleado.getHorasJornada()))
                .multiply(BigDecimal.valueOf(30));
        return salarioMensual.multiply(BigDecimal.valueOf(aniosTrabajados)).setScale(2, RoundingMode.HALF_UP);
    }

    public int mesesDesdeContratacion(Empleado empleado) {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate inicio = empleado.getFechaContratacion();
        int meses = (hoy.getYear() - inicio.getYear()) * 12 + (hoy.getMonthValue() - inicio.getMonthValue());
        return Math.max(0, meses);
    }

    public int aniosDesdeContratacion(Empleado empleado) {
        return mesesDesdeContratacion(empleado) / 12;
    }
}
