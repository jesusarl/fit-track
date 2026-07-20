package com.fittrack.dto;

import com.fittrack.model.ExerciseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateWorkoutRequest(
        @NotNull ExerciseType tipoEjercicio,
        @NotNull @Positive Double distanciaKm,
        @NotNull @Positive Integer tiempoMinutos,
        @NotNull LocalDate fecha,
        @NotNull Long usuarioId
) {
}
