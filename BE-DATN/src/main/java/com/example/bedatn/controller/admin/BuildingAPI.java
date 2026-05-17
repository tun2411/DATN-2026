package com.example.bedatn.controller.admin;

import com.example.bedatn.exception.ValidateDataBuildingException;
import com.example.bedatn.dto.request.BuildingRequest;
import com.example.bedatn.dto.request.BuildingSearchRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.BuildingListPageResponse;
import com.example.bedatn.dto.response.BuildingSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/api/buildings")
@RestController
public class BuildingAPI {

    @Autowired
    private BuildingService buildingService;

    /**
     * Danh sách BĐS (phân trang + lọc query giống admin search).
     * Ví dụ: GET /api/buildings?page=0&size=12&district=HOAN_KIEM
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BuildingListPageResponse>> listBuildings(
            @ModelAttribute BuildingSearchRequest searchRequest,
            @RequestParam(name = "publicView", defaultValue = "false") boolean publicView,
            @PageableDefault(size = 12, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        // publicView = true → trang sản phẩm công khai, không lọc theo staff
        if (!publicView) {
            MyUserDetail user = currentUserOrNull();
            boolean manager = hasRole(user, "MANAGER");
            if (user != null && !manager && hasRole(user, "STAFF")) {
                // STAFF chỉ xem các tòa nhà được phân công quản lý
                searchRequest.setStaffId(user.getId());
            }
        }
        List<BuildingSearchResponse> items = buildingService.searchBuildings(searchRequest, pageable);
        int total = buildingService.countTotalItems(searchRequest);
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();
        int totalPages = (total == 0 || size == 0) ? 0 : (total + size - 1) / size;

        BuildingListPageResponse body = new BuildingListPageResponse();
        body.setItems(items);
        body.setTotalItems(total);
        body.setPage(page);
        body.setPageSize(size);
        body.setTotalPages(totalPages);

        ApiResponse<BuildingListPageResponse> response = new ApiResponse<>();
        response.setMessage("OK");
        response.setData(body);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createBuilding(@Valid @RequestBody BuildingRequest buildingRequest) {
        var entity = buildingService.createBuilding(buildingRequest);
        ApiResponse<Long> body = new ApiResponse<>();
        body.setMessage("Create Building Completed");
        body.setData(entity.getId());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateBuilding(@Valid @RequestBody BuildingRequest buildingRequest) {
        if (buildingRequest.getId() == null) {
            throw new ValidateDataBuildingException("Building Id not be null");
        }
        MyUserDetail user = currentUser();
        boolean manager = hasRole(user, "MANAGER");
        if (!manager) {
            boolean staff = hasRole(user, "STAFF");
            if (!staff || !buildingService.checkAssignedStaff(buildingRequest.getId(), user.getId())) {
                throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa tòa nhà này");
            }
        }
        buildingService.updateBuilding(buildingRequest);
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Update Completed");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @DeleteMapping("/{ids}")
    public ResponseEntity<ApiResponse<Void>> deleteBuildings(@PathVariable List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidateDataBuildingException("List Building ID not be null");
        }
        buildingService.delete(ids);
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Delete Completed");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingRequest>> getBuildingDetail(@PathVariable Long id) {
        BuildingRequest detail = buildingService.findBuildingById(id);
        ApiResponse<BuildingRequest> body = new ApiResponse<>();
        body.setMessage("Completed");
        body.setData(detail);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/staffs")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> loadStaffs(@PathVariable Long id) {
        List<StaffResponse> staffResponseDTOS = buildingService.findAssignedStaffs(id);
        ApiResponse<List<StaffResponse>> body = new ApiResponse<>();
        body.setMessage("Completed");
        body.setData(staffResponseDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/staffs/list")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> listAllStaffs() {
        List<StaffResponse> staffList = buildingService.findAllStaffs();
        ApiResponse<List<StaffResponse>> body = new ApiResponse<>();
        body.setMessage("Completed");
        body.setData(staffList);
        return ResponseEntity.ok(body);
    }

    private static boolean hasRole(MyUserDetail user, String roleWithoutPrefix) {
        if (user == null) {
            return false;
        }
        String want = "ROLE_" + roleWithoutPrefix;
        for (GrantedAuthority a : user.getAuthorities()) {
            if (want.equalsIgnoreCase(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static MyUserDetail currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            return null;
        }
        return (MyUserDetail) auth.getPrincipal();
    }

    private static MyUserDetail currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (MyUserDetail) auth.getPrincipal();
    }
}
