package com.opsbridge.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AssignRequest(@NotBlank @Size(max=100) String assignedTo) {}
