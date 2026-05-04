package com.example.bedatn.controller.admin;

import com.example.bedatn.converter.TransactionConverter;
import com.example.bedatn.documents.AssignmentBuildingEntity;
import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.dto.request.TransactionRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.TransactionProgressResponse;
import com.example.bedatn.dto.response.TransactionResponse;
import com.example.bedatn.documents.TransactionEntity;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionAPI {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private TransactionConverter transactionConverter;
    @Autowired
    private AssignmentBuildingRepository assignmentBuildingRepository;
    @Autowired
    private BuildingRepository buildingRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        MyUserDetail user = currentUser();
        boolean manager = hasRole(user, "MANAGER");

        List<TransactionEntity> entities;
        if (manager) {
            entities = customerId != null
                    ? transactionService.findByCustomerId(customerId)
                    : transactionService.findAllTransactions();
        } else if (hasRole(user, "STAFF")) {
            Set<Long> allowedCustomers = allowedCustomerIdsForStaff(user.getId());
            if (customerId != null) {
                if (!allowedCustomers.contains(customerId)) {
                    throw new IllegalArgumentException("Bạn không có quyền xem giao dịch của khách hàng này");
                }
                entities = transactionService.findByCustomerId(customerId);
            } else {
                entities = transactionService.findByCustomerIds(List.copyOf(allowedCustomers));
            }
        } else {
            throw new IllegalArgumentException("Bạn không có quyền truy cập chức năng này");
        }

        List<TransactionResponse> items = entities.stream()
                .map(transactionConverter::toResponse)
                .collect(Collectors.toList());
        ApiResponse<List<TransactionResponse>> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(items);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        ApiResponse<TransactionResponse> response = new ApiResponse<>();
        try {
            enforceStaffPermission(transactionRequest.getCustomerId(), null);
            TransactionEntity transaction = transactionService.saveTransaction(transactionRequest);
            response.setData(transactionConverter.toResponse(transaction));
            response.setMessage("Thêm giao dịch thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Thêm giao dịch thất bại");
            response.setDetail(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(@PathVariable("id") Long id, @RequestBody TransactionRequest transactionRequest) {
        ApiResponse<TransactionResponse> response = new ApiResponse<>();
        try {
            enforceStaffPermission(transactionRequest.getCustomerId(), id);
            transactionRequest.setId(id);
            TransactionEntity transaction = transactionService.updateTransaction(id, transactionRequest);
            response.setData(transactionConverter.toResponse(transaction));
            response.setMessage("Cập nhật giao dịch thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Cập nhật giao dịch thất bại");
            response.setDetail(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable("id") Long id, @RequestParam(value = "customerId", required = false) Long customerId) {
        ApiResponse<Void> response = new ApiResponse<>();
        try {
            enforceStaffPermission(customerId, id);
            transactionService.deleteTransaction(id);
            response.setMessage("Xóa giao dịch thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Xóa giao dịch thất bại");
            response.setDetail(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/my-progress")
    public ResponseEntity<ApiResponse<List<TransactionProgressResponse>>> getMyProgress(@RequestParam("customerId") Long customerId) {
        ApiResponse<List<TransactionProgressResponse>> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(transactionService.getProgressByCustomerId(customerId));
        return ResponseEntity.ok(response);
    }

    private void enforceStaffPermission(Long customerIdFromRequest, Long transactionId) {
        MyUserDetail user = currentUser();
        if (hasRole(user, "MANAGER")) {
            return;
        }
        if (!hasRole(user, "STAFF")) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác giao dịch");
        }
        Long targetCustomerId = customerIdFromRequest;
        if (targetCustomerId == null && transactionId != null) {
            targetCustomerId = transactionService.findById(transactionId).getCustomerId();
        }
        if (targetCustomerId == null) {
            throw new IllegalArgumentException("Thiếu customerId để kiểm tra quyền cập nhật giao dịch");
        }
        Set<Long> allowedCustomerIds = allowedCustomerIdsForStaff(user.getId());
        if (!allowedCustomerIds.contains(targetCustomerId)) {
            throw new IllegalArgumentException("STAFF chỉ được cập nhật giao dịch của tòa nhà mình quản lý");
        }
    }

    private Set<Long> allowedCustomerIdsForStaff(Long staffId) {
        if (staffId == null) {
            return Collections.emptySet();
        }
        List<Long> buildingIds = assignmentBuildingRepository.findByStaffId(staffId).stream()
                .map(AssignmentBuildingEntity::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (buildingIds.isEmpty()) {
            return Collections.emptySet();
        }
        return buildingRepository.findByIdIn(buildingIds).stream()
                .map(BuildingEntity::getLinkedCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static boolean hasRole(MyUserDetail user, String roleWithoutPrefix) {
        String want = "ROLE_" + roleWithoutPrefix;
        for (GrantedAuthority a : user.getAuthorities()) {
            if (want.equalsIgnoreCase(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static MyUserDetail currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (MyUserDetail) auth.getPrincipal();
    }
}
