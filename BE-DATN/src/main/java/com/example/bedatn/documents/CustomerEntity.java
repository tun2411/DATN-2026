package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "customer")
@Getter
@Setter
public class CustomerEntity extends BaseEntity{

    private static final long serialVersionUID = -4988455421375043688L;

    private String fullName;

    private String phone;

    private String email;

    private String companyName;

    private String demand;

    private String status;

    @Field("is_Active")
    private Long active;

}
