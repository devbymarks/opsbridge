package com.opsbridge.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
    @NotBlank @Size(max=140) String title,
    @NotBlank @Size(max=2000) String description,
    @NotBlank @Size(max=80) String category,
    @NotNull IncidentPriority priority,
    @NotBlank @Size(max=100) String reporter
) {}
