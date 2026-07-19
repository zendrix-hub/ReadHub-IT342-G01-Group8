package com.readhub.bookmanagement.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class StudentDashboardStatsDto {
    private long totalBorrows;
    private long activeLoans;
    private long pendingRequests;
    private String favoriteCategory;
    private Map<String, Long> borrowingTrends;
}
