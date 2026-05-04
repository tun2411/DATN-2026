package com.example.bedatn.documents;

import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.enums.DocType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "legal_documents")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class LegalDocumentEntity {
    @Id
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
    private String uploadedBy;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectReason;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
