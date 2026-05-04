package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private Long id;
    private String userName;
    private String fullName;
    private String email;
    private String roleCode;
    private Long customerId;
}
