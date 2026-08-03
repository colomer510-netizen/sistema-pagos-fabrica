package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_empleado")
public class DocumentoEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 60)
    private String tipo;

    @Column(length = 300)
    private String descripcion;

    @Column(length = 100)
    private String contentType;

    @Basic(fetch = FetchType.LAZY)
    private byte[] archivo;

    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public byte[] getArchivo() { return archivo; }
    public void setArchivo(byte[] archivo) { this.archivo = archivo; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
