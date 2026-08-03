package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones")
public class EvaluacionDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false, length = 50)
    private String periodo;

    @Column(nullable = false)
    private LocalDate fechaEvaluacion = LocalDate.now();

    @Column(nullable = false)
    private Integer puntaje = 0;

    @Column(nullable = false, length = 20)
    private String calificacion = "SIN EVALUAR";

    @Column(length = 50)
    private String evaluador;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public String getCalificacionAutomatica() {
        if (puntaje == null) {
            return "SIN EVALUAR";
        }
        if (puntaje >= 90) {
            return "EXCELENTE";
        }
        if (puntaje >= 75) {
            return "BUENO";
        }
        if (puntaje >= 60) {
            return "REGULAR";
        }
        return "INSUFICIENTE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDate getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDate fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
    public String getCalificacion() { return calificacion; }
    public void setCalificacion(String calificacion) { this.calificacion = calificacion; }
    public String getEvaluador() { return evaluador; }
    public void setEvaluador(String evaluador) { this.evaluador = evaluador; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
