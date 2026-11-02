package com.subhasis.cloudops.repository;

import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
}
