package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "user")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    private static final long serialVersionUID = -4988455421375043688L;

    private String userName;

    private String fullName;

    private String password;

    private String phone;

    private Integer status;

    private String roleCode;

    private String email;

    @Transient
    private List<RoleEntity> roles;

}
