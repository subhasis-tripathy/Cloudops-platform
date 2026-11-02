package com.subhasis.cloudops.dto;

import java.util.List;
import java.util.Map;

public record DashboardStats(long total, Map<String, Long> byStatus, List<ProjectResponse> recent) {}