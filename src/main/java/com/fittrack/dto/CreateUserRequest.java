package com.fittrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email
) {
}
