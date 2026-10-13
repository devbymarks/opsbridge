package com.opsbridge.incident;

import jakarta.validation.constraints.NotNull;
public record UpdateStatusRequest(@NotNull IncidentStatus status) {}
