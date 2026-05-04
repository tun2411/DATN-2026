package com.example.bedatn.service;

import com.example.bedatn.dto.request.InquiryAssignRequest;
import com.example.bedatn.dto.request.InquiryRequest;
import com.example.bedatn.dto.request.InquiryStatusRequest;
import com.example.bedatn.dto.response.InquiryResponse;

import java.util.List;

public interface InquiryService {
    InquiryResponse createInquiry(InquiryRequest request);
    List<InquiryResponse> getInquiries(String status, Long assignedStaffId);
    InquiryResponse assignInquiry(Long id, InquiryAssignRequest request);
    InquiryResponse updateStatus(Long id, InquiryStatusRequest request);
}
