package com.example.bedatn.service;

import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.dto.request.CustomerRequest;
import com.example.bedatn.dto.request.CustomerSearchRequest;
import com.example.bedatn.dto.response.CustomerOptionResponse;
import com.example.bedatn.dto.response.CustomerSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    List<CustomerSearchResponse> searchCustomers(CustomerSearchRequest customerSearchRequest, Pageable pageable);
    boolean existsByPhone(String phone, Long id);
    boolean existsByEmail(String email, Long id);
    void contactUser(CustomerRequest request);
    CustomerRequest findCustomerById(Long id);
    CustomerEntity createCustomer(CustomerRequest request);
    CustomerEntity updateCustomer(CustomerRequest request);
    String delete(List<Long> ids);
    List<StaffResponse> findAssignedStaffs(Long id);
    int countTotalItems(CustomerSearchRequest customerSearchRequest);
    CustomerEntity findById(Long id);
    boolean checkAssignedStaff(Long customerId, Long staffId);

    /** Khách đang hoạt động — chọn gán BĐS */
    List<CustomerOptionResponse> listActiveOptions(Pageable pageable);
}
