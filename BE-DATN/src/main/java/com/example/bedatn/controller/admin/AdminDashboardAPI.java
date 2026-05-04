package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.response.AdminDashboardSummaryResponse;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardAPI {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardAPI(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> summary(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        ApiResponse<AdminDashboardSummaryResponse> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(adminDashboardService.getSummary(currentUser(), year, month));
        return ResponseEntity.ok(response);
    }

    private static MyUserDetail currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (MyUserDetail) auth.getPrincipal();
    }
}
