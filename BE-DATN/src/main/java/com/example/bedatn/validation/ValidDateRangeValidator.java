package com.example.bedatn.validation;

import com.example.bedatn.dto.request.LegalDocumentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, LegalDocumentRequest> {
    @Override
    public boolean isValid(LegalDocumentRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getIssueDate() == null || value.getExpireDate() == null) {
            return true;
        }
        return !value.getExpireDate().isBefore(value.getIssueDate());
    }
}
