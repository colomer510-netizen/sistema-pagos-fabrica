package com.fabrica.pagos.model;

public class AsistenciaResumen {

    private String codigo;
    private String nombreCompleto;
    private String cargo;
    private String departamento;
    private int horasTotales;
    private int diasTrabajados;

    public AsistenciaResumen() {
    }

    public AsistenciaResumen(String codigo, String nombreCompleto, String cargo, String departamento,
                             int horasTotales, int diasTrabajados) {
        this.codigo = codigo;
        this.nombreCompleto = nombreCompleto;
        this.cargo = cargo;
        this.departamento = departamento;
        this.horasTotales = horasTotales;
        this.diasTrabajados = diasTrabajados;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getHorasTotales() { return horasTotales; }
    public void setHorasTotales(int horasTotales) { this.horasTotales = horasTotales; }
    public int getDiasTrabajados() { return diasTrabajados; }
    public void setDiasTrabajados(int diasTrabajados) { this.diasTrabajados = diasTrabajados; }
}
