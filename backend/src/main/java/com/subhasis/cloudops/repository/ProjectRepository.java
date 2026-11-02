package com.subhasis.cloudops.repository;

import com.subhasis.cloudops.entity.Project;
import com.subhasis.cloudops.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    @Query("select p.status, count(p) from Project p where p.owner = :owner group by p.status")
    List<Object[]> countByStatusForOwner(@Param("owner") User owner);

    @Query("select p.status, count(p) from Project p group by p.status")
    List<Object[]> countByStatusAll();

    List<Project> findTop5ByOwnerOrderByCreatedAtDesc(User owner);

    List<Project> findTop5ByOrderByCreatedAtDesc();
}