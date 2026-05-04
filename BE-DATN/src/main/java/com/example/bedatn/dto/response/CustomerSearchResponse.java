package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSearchResponse extends AbstractResponse<Object> {
    private String fullName;
    private String phone;
    private String email;
    private String demand;
    private String status;
}
