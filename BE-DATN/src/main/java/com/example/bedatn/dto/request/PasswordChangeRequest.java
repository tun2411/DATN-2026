package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PasswordChangeRequest extends AbstractRequest<PasswordChangeRequest> implements Serializable {

    private static final long serialVersionUID = 8835146939192307340L;

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
