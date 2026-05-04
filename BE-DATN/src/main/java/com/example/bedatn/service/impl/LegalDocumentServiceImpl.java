package com.example.bedatn.service.impl;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.LegalDocumentEntity;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.dto.request.LegalDocumentRequest;
import com.example.bedatn.dto.response.LegalDocumentResponse;
import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.LegalDocumentRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.LegalDocumentService;
import com.example.bedatn.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LegalDocumentServiceImpl implements LegalDocumentService {
    private final LegalDocumentRepository legalDocumentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final String publicBaseUrl;

    public LegalDocumentServiceImpl(LegalDocumentRepository legalDocumentRepository,
                                    BuildingRepository buildingRepository,
                                    UserRepository userRepository,
                                    FileStorageService fileStorageService,
                                    @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.legalDocumentRepository = legalDocumentRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public LegalDocumentResponse upload(String buildingId, MultipartFile file, LegalDocumentRequest metadata, String uploadedBy) {
        BuildingEntity building = findBuilding(buildingId);
        String fileUrl = fileStorageService.upload(file, buildingId);
        LocalDateTime now = LocalDateTime.now();

        LegalDocumentEntity entity = new LegalDocumentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setBuildingId(buildingId);
        entity.setDocType(metadata.getDocType());
        entity.setStatus(DocStatus.PENDING);
        entity.setFileUrl(fileUrl);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileSize(formatFileSize(file.getSize()));
        entity.setIssueDate(metadata.getIssueDate());
        entity.setExpireDate(metadata.getExpireDate());
        entity.setIssuedBy(metadata.getIssuedBy());
        entity.setCertificateNumber(metadata.getCertificateNumber());
        entity.setUploadedBy(uploadedBy);
        entity.setNote(metadata.getNote());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        LegalDocumentEntity saved = legalDocumentRepository.save(entity);
        List<String> ids = building.getLegalDocumentIds() == null
                ? new ArrayList<>()
                : new ArrayList<>(building.getLegalDocumentIds());
        if (!ids.contains(saved.getId())) {
            ids.add(saved.getId());
            building.setLegalDocumentIds(ids);
            buildingRepository.save(building);
        }
        return toResponse(saved);
    }

    @Override
    public List<LegalDocumentResponse> listByBuilding(String buildingId) {
        findBuilding(buildingId);
        return legalDocumentRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LegalDocumentResponse getOne(String id) {
        return toResponse(findDocument(id));
    }

    @Override
    public LegalDocumentResponse verify(String id, String verifiedBy) {
        LegalDocumentEntity entity = findDocument(id);
        entity.setStatus(DocStatus.VERIFIED);
        entity.setVerifiedBy(verifiedBy);
        entity.setVerifiedAt(LocalDateTime.now());
        entity.setRejectReason(null);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(legalDocumentRepository.save(entity));
    }

    @Override
    public LegalDocumentResponse reject(String id, String reason) {
        LegalDocumentEntity entity = findDocument(id);
        entity.setStatus(DocStatus.REJECTED);
        entity.setRejectReason(reason);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(legalDocumentRepository.save(entity));
    }

    @Override
    public void delete(String id) {
        LegalDocumentEntity entity = findDocument(id);
        if (entity.getStatus() == DocStatus.VERIFIED || entity.getStatus() == DocStatus.EXPIRED) {
            throw new IllegalArgumentException("Không thể xóa hồ sơ đã được xác minh");
        }
        fileStorageService.delete(entity.getFileUrl());
        legalDocumentRepository.delete(entity);
        BuildingEntity building = findBuilding(entity.getBuildingId());
        if (building.getLegalDocumentIds() != null) {
            List<String> ids = new ArrayList<>(building.getLegalDocumentIds());
            ids.remove(entity.getId());
            building.setLegalDocumentIds(ids);
            buildingRepository.save(building);
        }
    }

    @Override
    public List<LegalDocumentResponse> expiringSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(Math.max(days, 0));
        return legalDocumentRepository.findByStatusAndExpireDateBetweenOrderByExpireDateAsc(DocStatus.VERIFIED, today, until)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BuildingEntity findBuilding(String buildingId) {
        try {
            Long id = Long.valueOf(buildingId);
            return buildingRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BuildingEntity không tồn tại"));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BuildingEntity không tồn tại");
        }
    }

    private LegalDocumentEntity findDocument(String id) {
        return legalDocumentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LegalDocumentEntity không tồn tại"));
    }

    private LegalDocumentResponse toResponse(LegalDocumentEntity entity) {
        LegalDocumentResponse response = new LegalDocumentResponse();
        response.setId(entity.getId());
        response.setBuildingId(entity.getBuildingId());
        response.setDocType(entity.getDocType());
        response.setStatus(entity.getStatus());
        response.setFileUrl(toFullUrl(entity.getFileUrl()));
        response.setFileName(entity.getFileName());
        response.setFileSize(entity.getFileSize());
        response.setIssueDate(entity.getIssueDate());
        response.setExpireDate(entity.getExpireDate());
        response.setIssuedBy(entity.getIssuedBy());
        response.setCertificateNumber(entity.getCertificateNumber());
        response.setUploadedByName(resolveUserName(entity.getUploadedBy()));
        response.setVerifiedByName(resolveUserName(entity.getVerifiedBy()));
        response.setVerifiedAt(entity.getVerifiedAt());
        response.setRejectReason(entity.getRejectReason());
        response.setNote(entity.getNote());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            UserEntity user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            if (user == null || user.getFullName() == null || user.getFullName().isBlank()) {
                return userId;
            }
            return user.getFullName();
        } catch (NumberFormatException ex) {
            return userId;
        }
    }

    private String toFullUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.startsWith("http")) {
            return fileUrl;
        }
        return publicBaseUrl.replaceAll("/$", "") + fileUrl;
    }

    private String formatFileSize(long bytes) {
        BigDecimal mb = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(1024L * 1024L), 1, RoundingMode.HALF_UP);
        return mb.stripTrailingZeros().toPlainString() + " MB";
    }
}
