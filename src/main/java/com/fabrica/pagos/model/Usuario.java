package com.fabrica.pagos.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column
    private Boolean activo = true;

    @Column
    private Integer intentosFallidos = 0;

    @Column
    private LocalDateTime fechaBloqueo;

    @Column
    private Boolean cambiarPassword = false;

    @Column
    private LocalDate fechaExpiracionPassword;

    @Column
    private LocalDateTime fechaUltimoLogin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(LocalDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
    public Boolean getCambiarPassword() { return cambiarPassword; }
    public void setCambiarPassword(Boolean cambiarPassword) { this.cambiarPassword = cambiarPassword; }
    public LocalDate getFechaExpiracionPassword() { return fechaExpiracionPassword; }
    public void setFechaExpiracionPassword(LocalDate fechaExpiracionPassword) { this.fechaExpiracionPassword = fechaExpiracionPassword; }
    public LocalDateTime getFechaUltimoLogin() { return fechaUltimoLogin; }
    public void setFechaUltimoLogin(LocalDateTime fechaUltimoLogin) { this.fechaUltimoLogin = fechaUltimoLogin; }
}
