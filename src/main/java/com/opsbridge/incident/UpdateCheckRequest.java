package com.opsbridge.incident;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record UpdateCheckRequest(@NotNull CheckStatus status, @Size(max=2000) String notes) {}
