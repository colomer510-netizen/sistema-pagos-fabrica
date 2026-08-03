package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "deducciones_aplicadas")
public class DeduccionAplicada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recibo_id", nullable = false)
    private Recibo recibo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recibo getRecibo() { return recibo; }
    public void setRecibo(Recibo recibo) { this.recibo = recibo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
