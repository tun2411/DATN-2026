package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AssignmentCustomerRequest {
    private Long customerId;
    private List<Long> staffs;
}
