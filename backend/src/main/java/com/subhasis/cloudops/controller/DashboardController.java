package com.subhasis.cloudops.controller;

import com.subhasis.cloudops.dto.DashboardStats;
import com.subhasis.cloudops.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping("/stats")
    public DashboardStats stats() { return service.getStats(); }
}