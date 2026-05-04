package com.example.bedatn.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest extends AbstractRequest<Object> {

    @NotBlank(message = "Họ và tên không được thiếu")
    private String fullName;
    @NotBlank(message = "Số điện thoại không được thiếu")
    private String phone;
    @Email(message = "Invalid email format")
    private String email;
    private String demand;
    private String status;
    private long staffId;
    private String companyName;
    private Long is_Active;
}
