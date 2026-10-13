package com.opsbridge.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreateCheckRequest(@NotBlank @Size(max=180) String title,
                                 @NotBlank @Size(max=1000) String expectedResult) {}
