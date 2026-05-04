package com.example.bedatn.dto.request;

import com.example.bedatn.enums.DocType;
import com.example.bedatn.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@ValidDateRange
@Getter
@Setter
public class LegalDocumentRequest {
    @NotNull(message = "docType không được để trống")
    private DocType docType;
    private String certificateNumber;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String issuedBy;
    private String note;
}
