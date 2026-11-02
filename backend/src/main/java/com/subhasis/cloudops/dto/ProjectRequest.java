package com.subhasis.cloudops.dto;

import com.subhasis.cloudops.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
        @NotBlank(message = "name is required") String name,
        String description,
        ProjectStatus status) {}
