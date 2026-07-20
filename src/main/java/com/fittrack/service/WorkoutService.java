package com.fittrack.service;

import com.fittrack.dto.CreateWorkoutRequest;
import com.fittrack.dto.UserWorkoutsResponse;
import com.fittrack.dto.WorkoutResponse;
import com.fittrack.model.User;
import com.fittrack.model.Workout;
import com.fittrack.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserService userService;

    public WorkoutService(WorkoutRepository workoutRepository, UserService userService) {
        this.workoutRepository = workoutRepository;
        this.userService = userService;
    }

    /**
     * Calcula el ritmo promedio en minutos por kilómetro (tiempo / distancia).
     */
    public static double calculateRitmoPromedio(int tiempoMinutos, double distanciaKm) {
        if (distanciaKm <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor que cero");
        }
        return (double) tiempoMinutos / distanciaKm;
    }

    @Transactional
    public WorkoutResponse createWorkout(CreateWorkoutRequest request) {
        User user = userService.findById(request.usuarioId());

        Workout workout = new Workout();
        workout.setTipoEjercicio(request.tipoEjercicio());
        workout.setDistanciaKm(request.distanciaKm());
        workout.setTiempoMinutos(request.tiempoMinutos());
        workout.setFecha(request.fecha());
        workout.setRitmoPromedio(calculateRitmoPromedio(request.tiempoMinutos(), request.distanciaKm()));
        workout.setUsuario(user);

        Workout saved = workoutRepository.save(workout);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserWorkoutsResponse getWorkoutsByUser(Long userId) {
        userService.findById(userId);

        List<Workout> workouts = workoutRepository.findByUsuarioIdOrderByFechaDesc(userId);
        double distanciaTotal = workouts.stream()
                .mapToDouble(Workout::getDistanciaKm)
                .sum();
        int tiempoTotal = workouts.stream()
                .mapToInt(Workout::getTiempoMinutos)
                .sum();

        List<WorkoutResponse> workoutResponses = workouts.stream()
                .map(this::toResponse)
                .toList();

        return new UserWorkoutsResponse(userId, distanciaTotal, tiempoTotal, workoutResponses);
    }

    private WorkoutResponse toResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getTipoEjercicio(),
                workout.getDistanciaKm(),
                workout.getTiempoMinutos(),
                workout.getFecha(),
                workout.getRitmoPromedio(),
                workout.getUsuario().getId()
        );
    }
}
