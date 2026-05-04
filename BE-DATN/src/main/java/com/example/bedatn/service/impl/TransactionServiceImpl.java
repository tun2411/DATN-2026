package com.example.bedatn.service.impl;

import com.example.bedatn.converter.TransactionConverter;
import com.example.bedatn.documents.LegalDocumentEntity;
import com.example.bedatn.dto.request.TransactionRequest;
import com.example.bedatn.dto.response.TransactionProgressResponse;
import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.documents.TransactionEntity;
import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.enums.DocType;
import com.example.bedatn.enums.TransactionStatus;
import com.example.bedatn.repository.LegalDocumentRepository;
import com.example.bedatn.repository.TransactionRepository;
import com.example.bedatn.service.CustomerService;
import com.example.bedatn.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements ITransactionService {
    private static final List<String> VALID_CODES = List.of(
            "NEW", "CONSULTING", "NEGOTIATING", "CONTRACTED", "COMPLETED"
    );

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionConverter transactionConverter;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Override
    public TransactionEntity saveTransaction(TransactionRequest transactionRequest) {
        TransactionEntity transaction = transactionConverter.toEntity(transactionRequest);
        if (transaction.getId() == null) {
            transaction.setId(System.currentTimeMillis());
        }
        if (transaction.getCreatedDate() == null) {
            transaction.setCreatedDate(new Date());
        }
        if (transactionRequest.getCustomerId() != null) {
            CustomerEntity customer = customerService.findById(transactionRequest.getCustomerId());
            if (customer != null) {
                transaction.setCustomerId(customer.getId());
            } else {
                throw new IllegalArgumentException("Customer not found with id: " + transactionRequest.getCustomerId());
            }
        }
        transaction.setCode(normalizeCode(transactionRequest.getCode()));
        applyDefaults(transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    public TransactionEntity updateTransaction(Long id, TransactionRequest transactionRequest) {
        TransactionEntity transaction = transactionConverter.toEntity(transactionRequest);
        TransactionEntity transactionEntity = transactionRepository.findById(id).get();
        Date createdDate = transactionEntity.getCreatedDate();
        String createdBy = transactionEntity.getCreatedBy();
        transaction.setCreatedBy(createdBy);
        transaction.setCreatedDate(createdDate);
        transaction.setId(id);
        if (transactionRequest.getCustomerId() != null) {
            CustomerEntity customer = customerService.findById(transactionRequest.getCustomerId());
            if (customer != null) {
                transaction.setCustomerId(customer.getId());
            } else {
                throw new IllegalArgumentException("Customer not found with id: " + transactionRequest.getCustomerId());
            }
        }
        transaction.setCode(normalizeCode(transactionRequest.getCode()));
        applyDefaults(transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    public TransactionEntity findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
    }

    @Override
    public List<TransactionEntity> findAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<TransactionEntity> findByCustomerId(Long customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    @Override
    public List<TransactionEntity> findByCustomerIds(List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findByCustomerIdIn(customerIds);
    }

    @Override
    public List<TransactionEntity> findByStaffId(Long staffId) {
        return transactionRepository.findByStaffId(staffId);
    }

    @Override
    public List<TransactionEntity> getTransactionByCodeAndCustomerId(String code, Long customerId) {
        if (code == null || customerId == null) {
            throw new IllegalArgumentException("Code and customerId must not be null");
        }
        return transactionRepository.findByCodeAndCustomerId(code, customerId);
    }

    @Override
    public List<TransactionProgressResponse> getProgressByCustomerId(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        return transactionRepository.findByCustomerId(customerId).stream()
                .map(this::toProgressResponse)
                .collect(Collectors.toList());
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "NEW";
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (!VALID_CODES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid transaction code: " + code);
        }
        return normalized;
    }

    private void applyDefaults(TransactionEntity transaction) {
        if (transaction.getStatus() == null) {
            transaction.setStatus(TransactionStatus.PENDING);
        }
    }

    private TransactionProgressResponse toProgressResponse(TransactionEntity entity) {
        TransactionProgressResponse item = new TransactionProgressResponse();
        item.setId(entity.getId());
        item.setCustomerId(entity.getCustomerId());
        item.setStaffId(entity.getStaffId());
        item.setBuildingId(entity.getBuildingId());
        item.setCode(entity.getCode());
        item.setNote(entity.getNote());
        enrichLegalDocumentUrls(item, entity);

        String code = entity.getCode() == null ? "NEW" : entity.getCode().trim().toUpperCase(Locale.ROOT);
        if (!VALID_CODES.contains(code)) {
            code = "NEW";
        }
        switch (code) {
            case "NEW":
                item.setProgressPercent(10);
                item.setLabel("Moi tao");
                break;
            case "CONSULTING":
                item.setProgressPercent(30);
                item.setLabel("Dang tu van");
                break;
            case "NEGOTIATING":
                item.setProgressPercent(60);
                item.setLabel("Dang dam phan");
                break;
            case "CONTRACTED":
                item.setProgressPercent(85);
                item.setLabel("Da ky hop dong");
                break;
            case "COMPLETED":
                item.setProgressPercent(100);
                item.setLabel("Hoan tat");
                break;
            default:
                item.setProgressPercent(0);
                item.setLabel("Khong xac dinh");
        }
        return item;
    }

    private void enrichLegalDocumentUrls(TransactionProgressResponse item, TransactionEntity entity) {
        if (entity.getBuildingId() == null) {
            return;
        }
        String buildingId = String.valueOf(entity.getBuildingId());
        item.setCertificateFileUrl(resolveCertificateUrl(buildingId));
        String code = entity.getCode() == null ? "" : entity.getCode().trim().toUpperCase(Locale.ROOT);
        if ("NEGOTIATING".equals(code) || "CONTRACTED".equals(code)) {
            item.setDepositContractFileUrl(resolveDocumentUrl(buildingId, DocType.HOP_DONG_DAT_COC));
        }
        if ("COMPLETED".equals(code)) {
            item.setSaleContractFileUrl(resolveDocumentUrl(buildingId, DocType.HOP_DONG_MUA_BAN));
        }
    }

    private String resolveCertificateUrl(String buildingId) {
        String url = resolveDocumentUrl(buildingId, DocType.GIAY_CHUNG_NHAN);
        if (url != null) {
            return url;
        }
        url = resolveDocumentUrl(buildingId, DocType.SO_DO);
        if (url != null) {
            return url;
        }
        return resolveDocumentUrl(buildingId, DocType.SO_HONG);
    }

    private String resolveDocumentUrl(String buildingId, DocType docType) {
        LegalDocumentEntity document = legalDocumentRepository
                .findFirstByBuildingIdAndDocTypeAndStatusOrderByCreatedAtDesc(buildingId, docType, DocStatus.VERIFIED);
        return document == null ? null : toFullUrl(document.getFileUrl());
    }

    private String toFullUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.startsWith("http")) {
            return fileUrl;
        }
        return publicBaseUrl.replaceAll("/$", "") + fileUrl;
    }
}
