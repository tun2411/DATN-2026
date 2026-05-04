package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private T data;
    private String message;
    private String detail;
}
