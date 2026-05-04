package com.example.bedatn.controller.admin;

import com.example.bedatn.exception.ValidateDataCustomerException;
import com.example.bedatn.dto.request.CustomerRequest;
import com.example.bedatn.dto.request.CustomerSearchRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.CustomerListPageResponse;
import com.example.bedatn.dto.response.CustomerOptionResponse;
import com.example.bedatn.dto.response.CustomerSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import com.example.bedatn.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/api/customers")
@RestController
public class CustomerAPI {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<CustomerListPageResponse>> listCustomers(
            @ModelAttribute CustomerSearchRequest searchRequest,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        searchRequest.setIs_Active(1L);
        List<CustomerSearchResponse> items = customerService.searchCustomers(searchRequest, pageable);
        int total = customerService.countTotalItems(searchRequest);
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();
        int totalPages = (total == 0 || size == 0) ? 0 : (total + size - 1) / size;

        CustomerListPageResponse data = new CustomerListPageResponse();
        data.setItems(items);
        data.setTotalItems(total);
        data.setPage(page);
        data.setPageSize(size);
        data.setTotalPages(totalPages);

        ApiResponse<CustomerListPageResponse> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerRequest>> getCustomerDetail(@PathVariable Long id) {
        CustomerRequest detail = customerService.findCustomerById(id);
        ApiResponse<CustomerRequest> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(detail);
        return ResponseEntity.ok(response);
    }

    /** Danh sách khách đang hoạt động (dropdown gán BĐS) */
    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<CustomerOptionResponse>>> listActiveOptions(
            @PageableDefault(size = 500) Pageable pageable) {
        List<CustomerOptionResponse> data = customerService.listActiveOptions(pageable);
        ApiResponse<List<CustomerOptionResponse>> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        ApiResponse<Void> response = new ApiResponse<>();
        try {
            customerService.createCustomer(request);
            response.setMessage("Create Customer Completed");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            response.setMessage("Failed to create customer");
            response.setDetail(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setMessage("An error occurred");
            response.setDetail(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateCustomer(@Valid @RequestBody CustomerRequest request) {
        if (request.getId() == null) {
            throw new ValidateDataCustomerException("Customer Id not be null");
        }
        ApiResponse<Void> response = new ApiResponse<>();
        try {
            customerService.updateCustomer(request);
            response.setMessage("Update Completed");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            response.setMessage("Failed to update customer");
            response.setDetail(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setMessage("An error occurred");
            response.setDetail(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{ids}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomers(@PathVariable List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidateDataCustomerException("List Customer ID not be null");
        }
        customerService.delete(ids);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Delete Completed");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/staffs")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> loadStaffs(@PathVariable Long id) {
        List<StaffResponse> staffResponseDTOS = customerService.findAssignedStaffs(id);
        ApiResponse<List<StaffResponse>> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(staffResponseDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
