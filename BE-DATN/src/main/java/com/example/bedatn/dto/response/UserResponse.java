package com.example.bedatn.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserResponse extends AbstractResponse<UserResponse> {
    private String userName;
    private String fullName;
    @JsonIgnore
    private String password;
    private String email;
    private Integer status;
    private List<RoleResponse> roles = new ArrayList<>();
    private String roleName;
    private String roleCode;
    private Map<String, String> roleDTOs = new HashMap<>();
}
