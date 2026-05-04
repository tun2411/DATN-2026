package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "role")
@Getter
@Setter
public class RoleEntity extends BaseEntity {

    private static final long serialVersionUID = -6525302831793188081L;

    private String name;

    private String code;

}
