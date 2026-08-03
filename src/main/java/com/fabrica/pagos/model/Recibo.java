package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recibos")
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nomina_id", nullable = false)
    private Nomina nomina;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private Integer horasNormales;

    @Column(nullable = false)
    private Integer horasExtras;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal salarioBruto;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal descuentoInss;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalDeducciones;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal salarioNeto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Nomina getNomina() { return nomina; }
    public void setNomina(Nomina nomina) { this.nomina = nomina; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public Integer getHorasNormales() { return horasNormales; }
    public void setHorasNormales(Integer horasNormales) { this.horasNormales = horasNormales; }
    public Integer getHorasExtras() { return horasExtras; }
    public void setHorasExtras(Integer horasExtras) { this.horasExtras = horasExtras; }
    public BigDecimal getSalarioBruto() { return salarioBruto; }
    public void setSalarioBruto(BigDecimal salarioBruto) { this.salarioBruto = salarioBruto; }
    public BigDecimal getDescuentoInss() { return descuentoInss; }
    public void setDescuentoInss(BigDecimal descuentoInss) { this.descuentoInss = descuentoInss; }
    public BigDecimal getTotalDeducciones() { return totalDeducciones; }
    public void setTotalDeducciones(BigDecimal totalDeducciones) { this.totalDeducciones = totalDeducciones; }
    public BigDecimal getSalarioNeto() { return salarioNeto; }
    public void setSalarioNeto(BigDecimal salarioNeto) { this.salarioNeto = salarioNeto; }
}
