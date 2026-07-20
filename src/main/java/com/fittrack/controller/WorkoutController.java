package com.fittrack.controller;

import com.fittrack.dto.CreateWorkoutRequest;
import com.fittrack.dto.UserWorkoutsResponse;
import com.fittrack.dto.WorkoutResponse;
import com.fittrack.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(@Valid @RequestBody CreateWorkoutRequest request) {
        return workoutService.createWorkout(request);
    }

    @GetMapping("/user/{userId}")
    public UserWorkoutsResponse getWorkoutsByUser(@PathVariable Long userId) {
        return workoutService.getWorkoutsByUser(userId);
    }
}
