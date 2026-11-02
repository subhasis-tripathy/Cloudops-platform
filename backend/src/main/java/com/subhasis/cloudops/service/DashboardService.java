package com.subhasis.cloudops.service;

import com.subhasis.cloudops.dto.DashboardStats;
import com.subhasis.cloudops.dto.ProjectResponse;
import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.ProjectStatus;
import com.subhasis.cloudops.entity.Role;
import com.subhasis.cloudops.entity.User;
import com.subhasis.cloudops.repository.ProjectRepository;
import com.subhasis.cloudops.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projects;
    private final UserRepository users;

    public DashboardService(ProjectRepository projects, UserRepository users) {
        this.projects = projects;
        this.users = users;
    }

    public DashboardStats getStats() {
        User me = currentUser();
        boolean admin = me.getRole() == Role.ADMIN;

        // Start every status at 0 so the frontend always gets all three keys
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ProjectStatus s : ProjectStatus.values()) byStatus.put(s.name(), 0L);

        List<Object[]> rows = admin ? projects.countByStatusAll() : projects.countByStatusForOwner(me);
        long total = 0;
        for (Object[] row : rows) {
            if (row[0] == null) continue;
            long count = (Long) row[1];
            byStatus.put(((ProjectStatus) row[0]).name(), count);
            total += count;
        }

        List<Project> recent = admin
                ? projects.findTop5ByOrderByCreatedAtDesc()
                : projects.findTop5ByOwnerOrderByCreatedAtDesc(me);

        return new DashboardStats(total, byStatus, recent.stream().map(ProjectResponse::from).toList());
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow();
    }
}