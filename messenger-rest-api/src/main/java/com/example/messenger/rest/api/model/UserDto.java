package com.example.messenger.rest.api.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserDto(
    @NotNull
    @NotEmpty
    String firstName,
    @NotNull
    @NotEmpty
    String lastName,
    @NotNull
    @NotEmpty
    String email
) {
}
