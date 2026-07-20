package com.fittrack.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkoutServiceTest {

    @ParameterizedTest
    @CsvSource({
            "30, 5.0, 6.0",
            "45, 10.0, 4.5",
            "60, 15.0, 4.0",
            "20, 4.0, 5.0"
    })
    void calculateRitmoPromedio_returnsTiempoDivididoDistancia(
            int tiempoMinutos, double distanciaKm, double expectedRitmo) {
        double ritmo = WorkoutService.calculateRitmoPromedio(tiempoMinutos, distanciaKm);
        assertEquals(expectedRitmo, ritmo, 0.001);
    }

    @Test
    void calculateRitmoPromedio_throwsWhenDistanciaIsZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> WorkoutService.calculateRitmoPromedio(30, 0)
        );
        assertEquals("La distancia debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void calculateRitmoPromedio_throwsWhenDistanciaIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WorkoutService.calculateRitmoPromedio(30, -5.0)
        );
    }
}
