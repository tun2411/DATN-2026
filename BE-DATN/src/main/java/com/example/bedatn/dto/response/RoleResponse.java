package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RoleResponse extends AbstractResponse<RoleResponse> implements Serializable {

    private static final long serialVersionUID = 5830885581031027382L;

    private String name;
    private String code;
}
