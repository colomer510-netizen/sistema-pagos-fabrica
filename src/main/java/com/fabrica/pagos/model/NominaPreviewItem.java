package com.fabrica.pagos.model;

import java.math.BigDecimal;
import java.util.List;

public class NominaPreviewItem {

    private Long empleadoId;
    private String codigo;
    private String nombreCompleto;
    private String cargo;
    private String departamento;
    private int horasNormales;
    private int horasExtras;
    private BigDecimal salarioBruto;
    private BigDecimal inss;
    private List<DeduccionPreview> deducciones;
    private BigDecimal cuotaPrestamo;
    private BigDecimal totalDeducciones;
    private BigDecimal salarioNeto;

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getHorasNormales() { return horasNormales; }
    public void setHorasNormales(int horasNormales) { this.horasNormales = horasNormales; }
    public int getHorasExtras() { return horasExtras; }
    public void setHorasExtras(int horasExtras) { this.horasExtras = horasExtras; }
    public BigDecimal getSalarioBruto() { return salarioBruto; }
    public void setSalarioBruto(BigDecimal salarioBruto) { this.salarioBruto = salarioBruto; }
    public BigDecimal getInss() { return inss; }
    public void setInss(BigDecimal inss) { this.inss = inss; }
    public List<DeduccionPreview> getDeducciones() { return deducciones; }
    public void setDeducciones(List<DeduccionPreview> deducciones) { this.deducciones = deducciones; }
    public BigDecimal getCuotaPrestamo() { return cuotaPrestamo; }
    public void setCuotaPrestamo(BigDecimal cuotaPrestamo) { this.cuotaPrestamo = cuotaPrestamo; }
    public BigDecimal getTotalDeducciones() { return totalDeducciones; }
    public void setTotalDeducciones(BigDecimal totalDeducciones) { this.totalDeducciones = totalDeducciones; }
    public BigDecimal getSalarioNeto() { return salarioNeto; }
    public void setSalarioNeto(BigDecimal salarioNeto) { this.salarioNeto = salarioNeto; }

    public static class DeduccionPreview {
        private final String nombre;
        private final BigDecimal monto;

        public DeduccionPreview(String nombre, BigDecimal monto) {
            this.nombre = nombre;
            this.monto = monto;
        }

        public String getNombre() { return nombre; }
        public BigDecimal getMonto() { return monto; }
    }
}
