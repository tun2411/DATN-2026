package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignmentBuildingRequest {
    private Long buildingId;
    private List<Long> staffs;
    private Long staffId;
}
