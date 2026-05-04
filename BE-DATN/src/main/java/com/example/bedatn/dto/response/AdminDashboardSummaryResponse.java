package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdminDashboardSummaryResponse {
    private int year;
    private int month;
    private int previousYear;
    private int previousMonth;
    private long totalBuildings;
    private long totalCustomers;
    private long totalAccounts;
    private long totalTransactions;
    private BigDecimal soldRevenue = BigDecimal.ZERO;
    private Map<String, Long> buildingsByStatus = new LinkedHashMap<>();
    private Map<String, Long> transactionsByStatus = new LinkedHashMap<>();
    private Map<String, DashboardMetricComparisonResponse> comparisons = new LinkedHashMap<>();
    private List<RecentTransactionResponse> recentTransactions = new ArrayList<>();
}
