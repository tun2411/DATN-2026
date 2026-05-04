package com.example.bedatn.dto.response;

import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.enums.DocType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class LegalDocumentResponse {
    private String id;
    private String buildingId;
    private DocType docType;
    private DocStatus status;
    private String fileUrl;
    private String fileName;
    private String fileSize;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String issuedBy;
    private String certificateNumber;
    private String uploadedByName;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String rejectReason;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
