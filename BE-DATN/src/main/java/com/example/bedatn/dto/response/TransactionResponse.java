package com.example.bedatn.dto.response;

import com.example.bedatn.enums.TransactionStatus;
import com.example.bedatn.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionResponse extends AbstractResponse<TransactionResponse> {
    private String code;
    private String note;
    private Long customerId;
    private Long staffId;
    private Long buildingId;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
}
