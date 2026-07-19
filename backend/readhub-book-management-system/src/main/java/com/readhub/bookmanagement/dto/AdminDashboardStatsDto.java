package com.readhub.bookmanagement.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AdminDashboardStatsDto {
    private long totalBooks;
    private long totalStudents;
    private long activeLoans;
    private long pendingApprovals;
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> borrowingTrends;
}
