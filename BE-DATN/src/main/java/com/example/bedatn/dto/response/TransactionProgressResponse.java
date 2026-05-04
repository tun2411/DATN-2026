package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionProgressResponse {
    private Long id;
    private Long customerId;
    private Long staffId;
    private String code;
    private String label;
    private String note;
    private int progressPercent;
    private Long buildingId;
    private String certificateFileUrl;
    private String depositContractFileUrl;
    private String saleContractFileUrl;
}
