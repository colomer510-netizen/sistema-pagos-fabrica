package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidaciones")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private LocalDate fechaSalida;

    @Column(nullable = false, length = 30)
    private String motivo;

    @Column(nullable = false)
    private Integer diasVacacionesPendientes = 0;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montoVacaciones = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal indemnizacion = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal otros = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 300)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Integer getDiasVacacionesPendientes() { return diasVacacionesPendientes; }
    public void setDiasVacacionesPendientes(Integer diasVacacionesPendientes) { this.diasVacacionesPendientes = diasVacacionesPendientes; }
    public BigDecimal getMontoVacaciones() { return montoVacaciones; }
    public void setMontoVacaciones(BigDecimal montoVacaciones) { this.montoVacaciones = montoVacaciones; }
    public BigDecimal getIndemnizacion() { return indemnizacion; }
    public void setIndemnizacion(BigDecimal indemnizacion) { this.indemnizacion = indemnizacion; }
    public BigDecimal getOtros() { return otros; }
    public void setOtros(BigDecimal otros) { this.otros = otros; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}
