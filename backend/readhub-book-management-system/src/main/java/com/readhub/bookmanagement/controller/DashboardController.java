package com.readhub.bookmanagement.controller;

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
    public ResponseEntity<AdminDashboardStatsDto> getAdminStats() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }

    @GetMapping("/student")
    public ResponseEntity<StudentDashboardStatsDto> getStudentStats(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(dashboardService.getStudentStats(email));
    }
}
