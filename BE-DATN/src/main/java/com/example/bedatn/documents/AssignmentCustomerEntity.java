package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "assignmentcustomer")
@Getter
@Setter
public class AssignmentCustomerEntity extends BaseEntity{

    private Long staffId;

    private Long customerId;

}
