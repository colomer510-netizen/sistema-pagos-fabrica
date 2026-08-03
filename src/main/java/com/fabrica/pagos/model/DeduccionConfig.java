package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "deducciones_config")
public class DeduccionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal porcentaje;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(length = 20)
    private String tipoCalculo = "PORCENTAJE";

    @Transient
    private BigDecimal montoCalculado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipoCalculo() { return tipoCalculo; }
    public void setTipoCalculo(String tipoCalculo) { this.tipoCalculo = tipoCalculo; }
    public BigDecimal getMontoCalculado() { return montoCalculado; }
    public void setMontoCalculado(BigDecimal montoCalculado) { this.montoCalculado = montoCalculado; }
}
