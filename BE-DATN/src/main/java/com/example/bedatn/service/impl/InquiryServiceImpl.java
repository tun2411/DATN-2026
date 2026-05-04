package com.example.bedatn.service.impl;

import com.example.bedatn.documents.InquiryEntity;
import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.dto.request.InquiryAssignRequest;
import com.example.bedatn.dto.request.InquiryRequest;
import com.example.bedatn.dto.request.InquiryStatusRequest;
import com.example.bedatn.dto.response.InquiryResponse;
import com.example.bedatn.repository.InquiryRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.InquiryService;
import com.example.bedatn.utils.VietnamPhoneUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class InquiryServiceImpl implements InquiryService {

    private static final List<String> VALID_TYPES = Arrays.asList("INTEREST", "CALLBACK");
    private static final List<String> VALID_STATUS = Arrays.asList("NEW", "ASSIGNED", "CLOSED");

    private final InquiryRepository inquiryRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final InquiryNotificationPublisher notificationPublisher;

    public InquiryServiceImpl(InquiryRepository inquiryRepository, BuildingRepository buildingRepository,
                              UserRepository userRepository, ModelMapper modelMapper,
                              InquiryNotificationPublisher notificationPublisher) {
        this.inquiryRepository = inquiryRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public InquiryResponse createInquiry(InquiryRequest request) {
        validateCreateRequest(request);
        // Không dùng ModelMapper InquiryRequest -> InquiryEntity: cả buildingId và customerId đều kết thúc bằng "Id",
        // ModelMapper có thể map nhầm cả hai vào BaseEntity.id và gây ConfigurationException.
        InquiryEntity entity = new InquiryEntity();
        entity.setBuildingId(request.getBuildingId());
        entity.setCustomerId(request.getCustomerId());
        entity.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        entity.setPhone(VietnamPhoneUtils.normalize(request.getPhone()));
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            entity.setEmail(request.getEmail().trim());
        }
        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            entity.setNote(request.getNote().trim());
        }
        entity.setId(System.currentTimeMillis());
        entity.setType(normalizeType(request.getType()));
        entity.setStatus("ASSIGNED");
        entity.setAssignedStaffId(null);
        InquiryEntity saved = inquiryRepository.save(entity);
        notificationPublisher.publishCreated(saved);
        return toResponse(saved);
    }

    @Override
    public List<InquiryResponse> getInquiries(String status, Long assignedStaffId) {
        List<InquiryEntity> entities;
        if (assignedStaffId != null) {
            entities = inquiryRepository.findByAssignedStaffIdOrderByCreatedDateDesc(assignedStaffId);
        } else if (status != null && !status.trim().isEmpty()) {
            String normalizedStatus = normalizeStatus(status);
            if ("ASSIGNED".equals(normalizedStatus)) {
                entities = inquiryRepository.findByStatusInOrderByCreatedDateDesc(List.of("NEW", "ASSIGNED"));
            } else {
                entities = inquiryRepository.findByStatusOrderByCreatedDateDesc(normalizedStatus);
            }
        } else {
            entities = inquiryRepository.findAll().stream()
                    .sorted(Comparator.comparing(InquiryEntity::getCreatedDate, Comparator.nullsLast(Date::compareTo)).reversed())
                    .collect(Collectors.toList());
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public InquiryResponse assignInquiry(Long id, InquiryAssignRequest request) {
        if (request == null || request.getStaffId() == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        UserEntity staff = userRepository.findById(request.getStaffId()).orElse(null);
        if (staff == null || !"STAFF".equalsIgnoreCase(staff.getRoleCode())) {
            throw new IllegalArgumentException("staffId is invalid or not STAFF role");
        }
        InquiryEntity entity = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        entity.setAssignedStaffId(request.getStaffId());
        entity.setStatus("ASSIGNED");
        InquiryEntity saved = inquiryRepository.save(entity);
        notificationPublisher.publishUpdated(saved, "INQUIRY_ASSIGNED");
        return toResponse(saved);
    }

    @Override
    public InquiryResponse updateStatus(Long id, InquiryStatusRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("status is required");
        }
        InquiryEntity entity = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        entity.setStatus(normalizeStatus(request.getStatus()));
        InquiryEntity saved = inquiryRepository.save(entity);
        notificationPublisher.publishUpdated(saved, "INQUIRY_STATUS_UPDATED");
        return toResponse(saved);
    }

    private String normalizeType(String type) {
        String value = (type == null ? "" : type.trim().toUpperCase(Locale.ROOT));
        if (!VALID_TYPES.contains(value)) {
            throw new IllegalArgumentException("type must be INTEREST or CALLBACK");
        }
        return value;
    }

    private String normalizeStatus(String status) {
        String value = status.trim().toUpperCase(Locale.ROOT);
        if ("RECEIVED".equals(value) || "DA_TIEP_NHAN".equals(value)) {
            return "ASSIGNED";
        }
        if ("PROCESSED".equals(value) || "DA_XU_LY".equals(value)) {
            return "CLOSED";
        }
        if (!VALID_STATUS.contains(value)) {
            throw new IllegalArgumentException("status must be ASSIGNED or CLOSED");
        }
        if ("NEW".equals(value)) {
            return "ASSIGNED";
        }
        return value;
    }

    private void validateCreateRequest(InquiryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payload is invalid");
        }
        if (request.getBuildingId() == null) {
            throw new IllegalArgumentException("buildingId is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("phone is required");
        }
        if (!VietnamPhoneUtils.isValidMobile10Digits(request.getPhone())) {
            throw new IllegalArgumentException("phone is invalid");
        }
        normalizeType(request.getType());
    }

    private InquiryResponse toResponse(InquiryEntity entity) {
        InquiryResponse response = modelMapper.map(entity, InquiryResponse.class);
        if (entity.getBuildingId() != null) {
            BuildingEntity building = buildingRepository.findById(entity.getBuildingId()).orElse(null);
            response.setBuildingName(building != null ? building.getName() : null);
        }
        if (entity.getAssignedStaffId() != null) {
            UserEntity staff = userRepository.findById(entity.getAssignedStaffId()).orElse(null);
            response.setAssignedStaffName(staff != null ? staff.getFullName() : null);
        }
        return response;
    }
}


