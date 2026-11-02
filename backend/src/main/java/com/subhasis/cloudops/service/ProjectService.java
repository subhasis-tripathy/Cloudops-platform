package com.subhasis.cloudops.service;

import com.subhasis.cloudops.dto.ProjectRequest;
import com.subhasis.cloudops.dto.ProjectResponse;
import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.ProjectStatus;
import com.subhasis.cloudops.entity.Role;
import com.subhasis.cloudops.entity.User;
import com.subhasis.cloudops.exception.ResourceNotFoundException;
import com.subhasis.cloudops.repository.ProjectRepository;
import com.subhasis.cloudops.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;
    private final UserRepository users;

    public ProjectService(ProjectRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        User me = currentUser();
        List<Project> list = isAdmin(me) ? repo.findAll() : repo.findByOwner(me);
        return list.stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(findAccessible(id));
    }

    public ProjectResponse create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStatus(req.status() != null ? req.status() : ProjectStatus.ACTIVE);
        p.setOwner(currentUser());
        return ProjectResponse.from(repo.save(p));
    }

    public ProjectResponse update(Long id, ProjectRequest req) {
        Project p = findAccessible(id);
        p.setName(req.name());
        p.setDescription(req.description());
        if (req.status() != null) p.setStatus(req.status());
        return ProjectResponse.from(repo.save(p));
    }

    public void delete(Long id) {
        repo.delete(findAccessible(id));
    }

    // Someone else's project returns 404, not 403, so its existence isn't leaked
    private Project findAccessible(Long id) {
        User me = currentUser();
        Project p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
        if (!isAdmin(me) && (p.getOwner() == null || !p.getOwner().getId().equals(me.getId()))) {
            throw new ResourceNotFoundException("Project " + id + " not found");
        }
        return p;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow();
    }

    private boolean isAdmin(User u) { return u.getRole() == Role.ADMIN; }
}
