package com.example.bedatn.dto.response;

import com.example.bedatn.enums.TransactionStatus;
import com.example.bedatn.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class RecentTransactionResponse {
    private Long id;
    private String code;
    private String note;
    private Long customerId;
    private String customerName;
    private Long staffId;
    private Long buildingId;
    private String buildingName;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private Date createdDate;
}
