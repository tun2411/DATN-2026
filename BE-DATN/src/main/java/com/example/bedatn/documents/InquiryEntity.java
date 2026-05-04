package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inquiry")
@Getter
@Setter
public class InquiryEntity extends BaseEntity {
    private Long buildingId;
    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    /** INTEREST | CALLBACK */
    private String type;
    private String note;
    /** ASSIGNED (Đã tiếp nhận) | CLOSED (Đã xử lý). NEW is legacy data. */
    private String status;
    private Long assignedStaffId;
}
