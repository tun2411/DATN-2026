package com.example.bedatn.controller;

import com.example.bedatn.dto.request.LegalDocumentRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.LegalDocumentResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.BuildingService;
import com.example.bedatn.service.LegalDocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/legal-documents")
public class LegalDocumentController {
    private final LegalDocumentService legalDocumentService;
    private final BuildingService buildingService;

    public LegalDocumentController(LegalDocumentService legalDocumentService, BuildingService buildingService) {
        this.legalDocumentService = legalDocumentService;
        this.buildingService = buildingService;
    }

    @PostMapping("/buildings/{buildingId}/upload")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> upload(
            @PathVariable String buildingId,
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("metadata") LegalDocumentRequest metadata) {
        MyUserDetail user = currentUser();
        assertCanUpload(buildingId, user);
        ApiResponse<LegalDocumentResponse> response = new ApiResponse<>();
        response.setMessage("Created");
        response.setData(legalDocumentService.upload(buildingId, file, metadata, user.getId() == null ? null : String.valueOf(user.getId())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/buildings/{buildingId}")
    public ResponseEntity<ApiResponse<List<LegalDocumentResponse>>> listByBuilding(@PathVariable String buildingId) {
        ApiResponse<List<LegalDocumentResponse>> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(legalDocumentService.listByBuilding(buildingId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> getOne(@PathVariable String id) {
        ApiResponse<LegalDocumentResponse> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(legalDocumentService.getOne(id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> verify(@PathVariable String id) {
        ApiResponse<LegalDocumentResponse> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(legalDocumentService.verify(id, currentUserId()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> reject(@PathVariable String id,
                                                                     @Valid @RequestBody RejectRequest request) {
        ApiResponse<LegalDocumentResponse> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(legalDocumentService.reject(id, request.getReason()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        legalDocumentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<LegalDocumentResponse>>> expiringSoon(
            @RequestParam(value = "days", defaultValue = "30") int days) {
        ApiResponse<List<LegalDocumentResponse>> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(legalDocumentService.expiringSoon(days));
        return ResponseEntity.ok(response);
    }

    private static String currentUserId() {
        MyUserDetail user = currentUserOrNull();
        if (user == null || user.getId() == null) {
            return null;
        }
        return String.valueOf(user.getId());
    }

    private void assertCanUpload(String buildingId, MyUserDetail user) {
        if (hasRole(user, "MANAGER")) {
            return;
        }
        if (!hasRole(user, "STAFF") || user.getId() == null) {
            throw new IllegalArgumentException("Bạn không có quyền upload hồ sơ pháp lý.");
        }
        Long parsedBuildingId;
        try {
            parsedBuildingId = Long.valueOf(buildingId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("buildingId không hợp lệ.");
        }
        if (!buildingService.checkAssignedStaff(parsedBuildingId, user.getId())) {
            throw new IllegalArgumentException("Bạn chỉ được upload hồ sơ pháp lý cho tòa nhà được phân công quản lý.");
        }
    }

    private static boolean hasRole(MyUserDetail user, String roleWithoutPrefix) {
        if (user == null) {
            return false;
        }
        String want = "ROLE_" + roleWithoutPrefix;
        for (GrantedAuthority authority : user.getAuthorities()) {
            if (want.equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static MyUserDetail currentUser() {
        MyUserDetail user = currentUserOrNull();
        if (user == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return user;
    }

    private static MyUserDetail currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            return null;
        }
        return (MyUserDetail) auth.getPrincipal();
    }

    @Getter
    @Setter
    public static class RejectRequest {
        @NotBlank(message = "reason không được để trống")
        private String reason;
    }
}
