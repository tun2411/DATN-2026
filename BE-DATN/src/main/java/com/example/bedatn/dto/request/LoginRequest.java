package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    /** Username ho?c email */
    private String login;
    private String password;
}
