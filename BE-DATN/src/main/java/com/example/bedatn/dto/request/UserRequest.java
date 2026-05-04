package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserRequest extends AbstractRequest<Object> {
    private String userName;
    private String fullName;
    private String password;
    private String phone;
    private String email;
    private Integer status;
    private List<RoleRequest> roles = new ArrayList<>();
    private String roleName;
    private String roleCode;
    private Map<String, String> roleDTOs = new HashMap<>();
}
