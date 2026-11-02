package com.subhasis.cloudops.repository;

import com.subhasis.cloudops.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
