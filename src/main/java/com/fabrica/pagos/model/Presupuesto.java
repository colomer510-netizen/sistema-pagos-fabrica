package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Transient
    private BigDecimal gastoReal = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public BigDecimal getGastoReal() { return gastoReal; }
    public void setGastoReal(BigDecimal gastoReal) { this.gastoReal = gastoReal; }
    public BigDecimal getDisponible() {
        return (monto == null ? BigDecimal.ZERO : monto).subtract(gastoReal == null ? BigDecimal.ZERO : gastoReal);
    }
    public BigDecimal getPorcentajeUsado() {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return gastoReal.multiply(BigDecimal.valueOf(100))
                .divide(monto, 1, java.math.RoundingMode.HALF_UP);
    }
}
