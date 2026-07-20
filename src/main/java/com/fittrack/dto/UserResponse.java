package com.fittrack.dto;

public record UserResponse(
        Long id,
        String nombre,
        String email
) {
}
