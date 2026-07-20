package com.fittrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ejercicio", nullable = false)
    private ExerciseType tipoEjercicio;

    @Column(name = "distancia_km", nullable = false)
    private Double distanciaKm;

    @Column(name = "tiempo_minutos", nullable = false)
    private Integer tiempoMinutos;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "ritmo_promedio", nullable = false)
    private Double ritmoPromedio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    public Workout() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExerciseType getTipoEjercicio() {
        return tipoEjercicio;
    }

    public void setTipoEjercicio(ExerciseType tipoEjercicio) {
        this.tipoEjercicio = tipoEjercicio;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Integer getTiempoMinutos() {
        return tiempoMinutos;
    }

    public void setTiempoMinutos(Integer tiempoMinutos) {
        this.tiempoMinutos = tiempoMinutos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Double getRitmoPromedio() {
        return ritmoPromedio;
    }

    public void setRitmoPromedio(Double ritmoPromedio) {
        this.ritmoPromedio = ritmoPromedio;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}
