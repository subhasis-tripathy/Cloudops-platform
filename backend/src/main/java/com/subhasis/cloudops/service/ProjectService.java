package com.subhasis.cloudops.service;

import com.subhasis.cloudops.dto.ProjectRequest;
import com.subhasis.cloudops.dto.ProjectResponse;
import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.ProjectStatus;
import com.subhasis.cloudops.exception.ResourceNotFoundException;
import com.subhasis.cloudops.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;

    public ProjectService(ProjectRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        return repo.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(find(id));
    }

    public ProjectResponse create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStatus(req.status() != null ? req.status() : ProjectStatus.ACTIVE);
        return ProjectResponse.from(repo.save(p));
    }

    public ProjectResponse update(Long id, ProjectRequest req) {
        Project p = find(id);
        p.setName(req.name());
        p.setDescription(req.description());
        if (req.status() != null) p.setStatus(req.status());
        return ProjectResponse.from(repo.save(p));
    }

    public void delete(Long id) {
        repo.delete(find(id));
    }

    private Project find(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }
}
