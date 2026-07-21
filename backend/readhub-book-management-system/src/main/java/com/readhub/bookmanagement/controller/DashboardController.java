package com.readhub.bookmanagement.controller;

import com.readhub.bookmanagement.dto.ApiResponse;
import com.readhub.bookmanagement.dto.AdminDashboardStatsDto;
import com.readhub.bookmanagement.dto.StudentDashboardStatsDto;
import com.readhub.bookmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getAdminStats() {
        AdminDashboardStatsDto stats = dashboardService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Admin stats retrieved successfully"));
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentDashboardStatsDto>> getStudentStats(Authentication authentication) {
        String email = authentication.getName();
        StudentDashboardStatsDto stats = dashboardService.getStudentStats(email);
        return ResponseEntity.ok(ApiResponse.success(stats, "Student stats retrieved successfully"));
    }
}
