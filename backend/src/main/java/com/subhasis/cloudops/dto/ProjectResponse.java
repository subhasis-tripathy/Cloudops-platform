package com.subhasis.cloudops.dto;

import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectResponse(Long id, String name, String description,
                              ProjectStatus status, LocalDateTime createdAt, String ownerEmail) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getStatus(),
                p.getCreatedAt(), p.getOwner() != null ? p.getOwner().getEmail() : null);
    }
}
