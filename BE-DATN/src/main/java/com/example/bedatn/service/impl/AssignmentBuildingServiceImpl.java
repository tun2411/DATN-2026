package com.example.bedatn.service.impl;

import com.example.bedatn.documents.AssignmentBuildingEntity;
import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.dto.request.AssignmentBuildingRequest;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.AssignmentBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentBuildingServiceImpl implements AssignmentBuildingService {

    @Autowired
    private AssignmentBuildingRepository assignmentBuildingRepository;
    @Autowired
    private BuildingRepository buildingRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public String updateAssignment(AssignmentBuildingRequest request) {
        BuildingEntity building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tòa nhà"));
        assignmentBuildingRepository.deleteByBuildingId(request.getBuildingId());
        Optional<Long> selectedStaffId = getSelectedStaffId(request);
        if (selectedStaffId.isEmpty()) {
            building.setManagerName(null);
            building.setManagerPhone(null);
            buildingRepository.save(building);
            return "Success";
        }
        Long staffId = selectedStaffId.get();
        UserEntity staff = userRepository.findById(staffId).orElse(null);
        if (staff == null || staff.getStatus() == null || staff.getStatus() != 1 || !"STAFF".equalsIgnoreCase(staff.getRoleCode())) {
            throw new IllegalArgumentException("Staff không hợp lệ hoặc đã bị khóa");
        }
        AssignmentBuildingEntity assignmentBuildingEntity = new AssignmentBuildingEntity();
        assignmentBuildingEntity.setId(System.nanoTime());
        assignmentBuildingEntity.setBuildingId(request.getBuildingId());
        assignmentBuildingEntity.setStaffId(staffId);
        assignmentBuildingRepository.save(assignmentBuildingEntity);

        // Đồng bộ thông tin quản lý theo nhân viên được gán (không nhập tay từ frontend)
        building.setManagerName(staff.getFullName());
        building.setManagerPhone(staff.getPhone());
        buildingRepository.save(building);
        return "Success";
    }

    private Optional<Long> getSelectedStaffId(AssignmentBuildingRequest request) {
        if (request.getStaffId() != null) {
            return Optional.of(request.getStaffId());
        }
        List<Long> assigns = request.getStaffs() == null ? Collections.emptyList() : request.getStaffs();
        if (assigns.isEmpty()) {
            return Optional.empty();
        }
        if (assigns.size() > 1) {
            throw new IllegalArgumentException("Mỗi tòa nhà chỉ được gán đúng 1 nhân viên quản lý");
        }
        return Optional.ofNullable(assigns.get(0));
    }
}
