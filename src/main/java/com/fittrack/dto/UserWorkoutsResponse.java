package com.fittrack.dto;

import java.util.List;

public record UserWorkoutsResponse(
        Long userId,
        Double distanciaTotalKm,
        Integer tiempoTotalMinutos,
        List<WorkoutResponse> workouts
) {
}
