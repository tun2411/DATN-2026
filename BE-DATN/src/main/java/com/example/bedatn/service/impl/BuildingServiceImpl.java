package com.example.bedatn.service.impl;

import com.example.bedatn.converter.BuildingConverter;
import com.example.bedatn.documents.AssignmentBuildingEntity;
import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.enums.BuildingSaleStatus;
import com.example.bedatn.enums.BuildingTypeCode;
import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.enums.DocType;
import com.example.bedatn.enums.TransactionStatus;
import com.example.bedatn.enums.TransactionType;
import com.example.bedatn.dto.request.BuildingRequest;
import com.example.bedatn.dto.request.BuildingSearchRequest;
import com.example.bedatn.dto.response.BuildingSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import com.example.bedatn.utils.UploadFileUtils;

import java.util.Base64;
import org.springframework.data.domain.Pageable;
import com.example.bedatn.documents.TransactionEntity;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.CustomerRepository;
import com.example.bedatn.repository.LegalDocumentRepository;
import com.example.bedatn.repository.TransactionRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private BuildingConverter buildingConverter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentBuildingRepository assignmentBuildingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    @Override
    public List<BuildingSearchResponse> searchBuildings(BuildingSearchRequest buildingSearchRequest, Pageable pageable) {
        List<BuildingEntity> buildingEntities = buildingRepository.searchBuildings(buildingSearchRequest, pageable);
        List<BuildingSearchResponse> results = new ArrayList<>();
        for (BuildingEntity buildingEntity : buildingEntities) {
            BuildingSearchResponse buildingResponse = buildingConverter.toBuildingSearchResponse(buildingEntity);
            enrichSaleFields(buildingResponse, buildingEntity);
            results.add(buildingResponse);
        }
        return results;
    }

    private void enrichSaleFields(BuildingSearchResponse r, BuildingEntity e) {
        if (r.getSaleStatus() == null || r.getSaleStatus().isBlank()) {
            r.setSaleStatus(BuildingSaleStatus.FOR_SALE.name());
        }
        Long cid = e.getLinkedCustomerId();
        if (cid == null) {
            return;
        }
        customerRepository.findById(cid).ifPresent(c -> r.setLinkedCustomerSummary(formatCustomerBrief(c)));
    }

    private static String formatCustomerBrief(CustomerEntity c) {
        String name = c.getFullName() != null ? c.getFullName().trim() : "";
        String phone = c.getPhone() != null ? c.getPhone().trim() : "";
        if (name.isEmpty()) {
            return phone;
        }
        if (phone.isEmpty()) {
            return name;
        }
        return name + " — " + phone;
    }

    private void validateAndApplySaleRules(BuildingRequest req) {
        if (req.getTypeCode() == null) {
            throw new IllegalArgumentException("typeCode is required (CAN_HO, NGUYEN_CAN, DAT_NEN)");
        }
        String raw = req.getSaleStatus();
        if (raw == null || raw.trim().isEmpty()) {
            req.setSaleStatus(BuildingSaleStatus.FOR_SALE.name());
            raw = req.getSaleStatus();
        }
        BuildingSaleStatus st;
        try {
            st = BuildingSaleStatus.valueOf(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái bán không hợp lệ (FOR_SALE, DEPOSIT, SOLD)");
        }
        req.setSaleStatus(st.name());
        if (st == BuildingSaleStatus.FOR_SALE) {
            req.setLinkedCustomerId(null);
            req.setCustomerId(null);
            validateTypeSpecificFields(req);
            return;
        }
        String customerIdRaw = req.getCustomerId();
        if (customerIdRaw == null || customerIdRaw.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng khi trạng thái là đặt cọc hoặc đã bán.");
        }
        Long cid;
        try {
            cid = Long.parseLong(customerIdRaw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("customerId không hợp lệ.");
        }
        req.setLinkedCustomerId(cid);
        req.setCustomerId(String.valueOf(cid));
        CustomerEntity c = customerRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng đã chọn."));
        if (c.getActive() != null && c.getActive().longValue() == 0L) {
            throw new IllegalArgumentException("Khách hàng đã bị vô hiệu hóa, không thể gán.");
        }
        validateLegalDocumentsForSaleStatus(req.getId(), st);
        validateTypeSpecificFields(req);
    }

    private void validateLegalDocumentsForSaleStatus(Long buildingId, BuildingSaleStatus status) {
        if (buildingId == null) {
            throw new IllegalArgumentException("Vui lòng tạo bất động sản trước, sau đó upload hồ sơ pháp lý rồi mới chuyển trạng thái.");
        }
        String bid = String.valueOf(buildingId);
        if (!hasVerifiedCertificate(bid)) {
            throw new IllegalArgumentException("BĐS phải có Giấy chứng nhận đã xác minh trước khi chuyển sang đặt cọc hoặc đã bán.");
        }
        if (status == BuildingSaleStatus.DEPOSIT
                && !legalDocumentRepository.existsByBuildingIdAndDocTypeAndStatus(bid, DocType.HOP_DONG_DAT_COC, DocStatus.VERIFIED)) {
            throw new IllegalArgumentException("BĐS phải có Hợp đồng đặt cọc đã xác minh trước khi chuyển sang trạng thái đặt cọc.");
        }
        if (status == BuildingSaleStatus.SOLD
                && !legalDocumentRepository.existsByBuildingIdAndDocTypeAndStatus(bid, DocType.HOP_DONG_MUA_BAN, DocStatus.VERIFIED)) {
            throw new IllegalArgumentException("BĐS phải có Hợp đồng mua bán đã xác minh trước khi chuyển sang trạng thái đã bán.");
        }
    }

    private boolean hasVerifiedCertificate(String buildingId) {
        return legalDocumentRepository.existsByBuildingIdAndDocTypeAndStatus(buildingId, DocType.GIAY_CHUNG_NHAN, DocStatus.VERIFIED)
                || legalDocumentRepository.existsByBuildingIdAndDocTypeAndStatus(buildingId, DocType.SO_DO, DocStatus.VERIFIED)
                || legalDocumentRepository.existsByBuildingIdAndDocTypeAndStatus(buildingId, DocType.SO_HONG, DocStatus.VERIFIED);
    }

    private void validateTypeSpecificFields(BuildingRequest req) {
        BuildingTypeCode type = req.getTypeCode();
        switch (type) {
            case CAN_HO:
                requireNotBlank(req.getBuildingName(), "buildingName is required for CAN_HO");
                break;
            case NGUYEN_CAN:
                requireNotBlank(req.getStructure(), "structure is required for NGUYEN_CAN");
                break;
            case DAT_NEN:
                if (req.getLandType() == null) {
                    throw new IllegalArgumentException("landType is required for DAT_NEN");
                }
                if (req.getWidth() == null || req.getLength() == null) {
                    throw new IllegalArgumentException("width and length are required for DAT_NEN");
                }
                break;
            default:
                throw new IllegalArgumentException("Loại bất động sản không hợp lệ.");
        }
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public BuildingRequest findBuildingById(Long id) {
        BuildingEntity buildingEntity = buildingRepository.findById(id).get();
        return buildingConverter.toBuildingRequest(buildingEntity);
    }

    @Override
    public String delete(List<Long> ids) {
        List<AssignmentBuildingEntity> assignments = assignmentBuildingRepository.findByBuildingIdIn(ids);
        assignmentBuildingRepository.deleteAll(assignments);

        buildingRepository.deleteAllByIdIn(ids);

        return "Success";
    }

    @Override
    public List<StaffResponse> findAssignedStaffs(Long id) {
        List<UserEntity> staffList = userRepository.findByStatusAndRoleCode(1, "STAFF");
        Set<Long> assignedStaffIds = userService.getUsersByBuildingId(id).stream()
                .map(UserEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<StaffResponse> staffResponseDTOS = new ArrayList<>();
        for (UserEntity staff : staffList) {
            StaffResponse sr = new StaffResponse();
            sr.setStaffId(staff.getId());
            sr.setFullName(staff.getFullName());
            if (assignedStaffIds.contains(staff.getId())) {
                sr.setChecked("checked");
            } else {
                sr.setChecked("");
            }
            staffResponseDTOS.add(sr);
        }
        return staffResponseDTOS;
    }

    @Override
    public List<StaffResponse> findAllStaffs() {
        List<UserEntity> staffList = userRepository.findByStatusAndRoleCode(1, "STAFF");
        List<StaffResponse> result = new ArrayList<>();
        for (UserEntity staff : staffList) {
            StaffResponse sr = new StaffResponse();
            sr.setStaffId(staff.getId());
            sr.setFullName(staff.getFullName());
            sr.setChecked("");
            result.add(sr);
        }
        return result;
    }

    @Override
    public BuildingEntity createBuilding(BuildingRequest buildingRequest) {
        validateAndApplySaleRules(buildingRequest);
        BuildingEntity buildingEntity = buildingConverter.toBuildingEntity(buildingRequest);
        if (buildingEntity.getId() == null) {
            buildingEntity.setId(System.currentTimeMillis());
        }
        LocalDateTime now = LocalDateTime.now();
        buildingEntity.setCreatedAt(now);
        buildingEntity.setUpdatedAt(now);
        saveThumbnail(buildingRequest, buildingEntity);
        BuildingEntity saved = buildingRepository.save(buildingEntity);
        syncTransactionForSaleStatus(saved);
        return saved;
    }

    @Override
    public BuildingEntity updateBuilding(BuildingRequest buildingRequest) {
        validateAndApplySaleRules(buildingRequest);
        BuildingEntity entity = buildingRepository.findById(buildingRequest.getId()).get();
        BuildingEntity buildingEntity = buildingConverter.toBuildingEntity(buildingRequest);
        buildingEntity.setId(entity.getId());
        buildingEntity.setAvatar(entity.getAvatar());
        buildingEntity.setLegalDocumentIds(entity.getLegalDocumentIds());
        buildingEntity.setCreatedAt(entity.getCreatedAt());
        buildingEntity.setUpdatedAt(LocalDateTime.now());
        saveThumbnail(buildingRequest, buildingEntity);
        BuildingEntity saved = buildingRepository.save(buildingEntity);
        syncTransactionForSaleStatus(saved);
        return saved;
    }

    private void syncTransactionForSaleStatus(BuildingEntity building) {
        String status = building.getSaleStatus();
        Long customerId = building.getLinkedCustomerId();

        if (status == null || "FOR_SALE".equals(status) || customerId == null) {
            return;
        }

        String txCode;
        TransactionType txType;
        TransactionStatus txStatus;
        switch (status) {
            case "DEPOSIT":
                txCode = "NEGOTIATING";
                txType = TransactionType.DEPOSIT;
                txStatus = TransactionStatus.PROCESSING;
                break;
            case "SOLD":
                txCode = "COMPLETED";
                txType = TransactionType.SALE;
                txStatus = TransactionStatus.SUCCESS;
                break;
            default:
                return;
        }

        List<TransactionEntity> existing = transactionRepository.findByCustomerIdAndBuildingId(customerId, building.getId());
        Date now = new Date();
        if (existing.isEmpty()) {
            TransactionEntity tx = new TransactionEntity();
            tx.setId(System.currentTimeMillis());
            tx.setCreatedDate(now);
            tx.setCustomerId(customerId);
            tx.setBuildingId(building.getId());
            tx.setCode(txCode);
            tx.setTransactionType(txType);
            tx.setStatus(txStatus);
            tx.setAmount(building.getPrice());
            tx.setNote("Tự động tạo khi cập nhật trạng thái tòa nhà: " + building.getName());
            transactionRepository.save(tx);
        } else {
            TransactionEntity tx = existing.get(0);
            if (tx.getCreatedDate() == null) {
                tx.setCreatedDate(now);
            }
            tx.setCode(txCode);
            tx.setTransactionType(txType);
            tx.setStatus(txStatus);
            tx.setAmount(building.getPrice());
            tx.setNote("Cập nhật tự động theo trạng thái tòa nhà: " + building.getName());
            transactionRepository.save(tx);
        }
    }

    @Override
    public boolean checkAssignedStaff(Long buildingId, Long staffId) {
        if (buildingId == null || staffId == null) {
            throw new IllegalArgumentException("buildingId and staffId must not be null");
        }
        List<AssignmentBuildingEntity> assignments = assignmentBuildingRepository.findByBuildingId(buildingId);
        if (assignments == null || assignments.isEmpty()) {
            return false;
        }
        return assignments.stream().anyMatch(assignment -> Objects.equals(assignment.getStaffId(), staffId));
    }

    @Override
    public int countTotalItems(BuildingSearchRequest buildingSearchRequest) {
        return buildingRepository.countTotalItem(buildingSearchRequest);
    }

    @Override
    public void saveThumbnail(BuildingRequest buildingRequest, BuildingEntity buildingEntity) {
        // Avatar chính
        String imageBase64 = buildingRequest.getImageBase64();
        if (imageBase64 != null) {
            String path = "/building/" + buildingRequest.getImageName();
            if (buildingEntity.getAvatar() != null && !path.equals(buildingEntity.getAvatar())) {
                File file = new File("C://home/office" + buildingEntity.getAvatar());
                file.delete();
            }
            byte[] bytes = Base64.getDecoder().decode(imageBase64);
            UploadFileUtils uploadFileUtils = new UploadFileUtils();
            uploadFileUtils.writeOrUpdate(path, bytes);
            buildingEntity.setAvatar(path);
        }

        // Gallery: ảnh phụ mới upload
        List<String> newBase64List = buildingRequest.getImagesBase64();
        List<String> newNameList = buildingRequest.getImageNames();
        // Giữ lại danh sách ảnh cũ mà frontend gửi lại (chưa bị xóa)
        List<String> existingImages = buildingRequest.getImages();
        List<String> finalImages = new ArrayList<>();
        if (existingImages != null) {
            finalImages.addAll(existingImages);
        }
        if (newBase64List != null && newNameList != null) {
            UploadFileUtils uploadFileUtils = new UploadFileUtils();
            for (int i = 0; i < newBase64List.size() && i < newNameList.size(); i++) {
                String b64 = newBase64List.get(i);
                String name = newNameList.get(i);
                if (b64 != null && name != null && !b64.isEmpty()) {
                    String raw = b64.contains(",") ? b64.split(",")[1] : b64;
                    String imgPath = "/building/" + buildingEntity.getId() + "_" + System.currentTimeMillis() + "_" + name;
                    byte[] bytes = Base64.getDecoder().decode(raw);
                    uploadFileUtils.writeOrUpdate(imgPath, bytes);
                    finalImages.add(imgPath);
                }
            }
        }
        buildingEntity.setImages(finalImages.isEmpty() ? null : finalImages);
    }
}
