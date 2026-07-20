package com.fittrack.dto;

import com.fittrack.model.ExerciseType;

import java.time.LocalDate;

public record WorkoutResponse(
        Long id,
        ExerciseType tipoEjercicio,
        Double distanciaKm,
        Integer tiempoMinutos,
        LocalDate fecha,
        Double ritmoPromedio,
        Long usuarioId
) {
}
