package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RoleRequest extends AbstractRequest<RoleRequest> implements Serializable {

    private static final long serialVersionUID = 5830885581031027382L;

    private String name;
    private String code;
}
