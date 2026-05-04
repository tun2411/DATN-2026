package com.example.bedatn.documents;

import com.example.bedatn.enums.TransactionStatus;
import com.example.bedatn.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "transaction")
@Getter
@Setter
public class TransactionEntity extends BaseEntity{

    private String code;

    private String note;

    private Long staffId;

    private Long customerId;

    private Long buildingId;

    private TransactionType transactionType;

    private TransactionStatus status;

    private BigDecimal amount;

}
