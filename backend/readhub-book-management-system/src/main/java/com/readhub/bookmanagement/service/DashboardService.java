package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.dto.AdminDashboardStatsDto;
import com.readhub.bookmanagement.dto.StudentDashboardStatsDto;

public interface DashboardService {
    AdminDashboardStatsDto getAdminStats();
    StudentDashboardStatsDto getStudentStats(String email);
}
