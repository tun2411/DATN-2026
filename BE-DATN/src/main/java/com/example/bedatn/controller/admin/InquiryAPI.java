package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.request.InquiryAssignRequest;
import com.example.bedatn.dto.request.InquiryRequest;
import com.example.bedatn.dto.request.InquiryStatusRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.InquiryResponse;
import com.example.bedatn.service.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryAPI {

    private final InquiryService inquiryService;

    public InquiryAPI(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> create(@RequestBody InquiryRequest request) {
        InquiryResponse data = inquiryService.createInquiry(request);
        ApiResponse<InquiryResponse> response = new ApiResponse<>();
        response.setMessage("Inquiry created");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "assignedStaffId", required = false) Long assignedStaffId) {
        ApiResponse<List<InquiryResponse>> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(inquiryService.getInquiries(status, assignedStaffId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<InquiryResponse>> assign(@PathVariable Long id, @RequestBody InquiryAssignRequest request) {
        ApiResponse<InquiryResponse> response = new ApiResponse<>();
        response.setMessage("Assigned");
        response.setData(inquiryService.assignInquiry(id, request));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateStatus(@PathVariable Long id, @RequestBody InquiryStatusRequest request) {
        ApiResponse<InquiryResponse> response = new ApiResponse<>();
        response.setMessage("Updated");
        response.setData(inquiryService.updateStatus(id, request));
        return ResponseEntity.ok(response);
    }
}
