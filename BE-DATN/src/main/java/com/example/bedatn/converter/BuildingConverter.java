package com.example.bedatn.converter;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.enums.BuildingSaleStatus;
import com.example.bedatn.enums.BuildingTypeCode;
import com.example.bedatn.enums.District;
import com.example.bedatn.dto.request.BuildingRequest;
import com.example.bedatn.dto.response.BuildingSearchResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BuildingConverter {
    @Autowired
    private ModelMapper modelMapper;

    public BuildingEntity toBuildingEntity(BuildingRequest buildingRequest) {
        BuildingEntity buildingEntityNew = modelMapper.map(buildingRequest, BuildingEntity.class);
        if (buildingRequest.getTypeCode() != null) {
            buildingEntityNew.setType(buildingRequest.getTypeCode().name());
        }
        if (buildingRequest.getRentArea() != null && !buildingRequest.getRentArea().trim().isEmpty()) {
            List<Long> rentAreaValues = Arrays.stream(buildingRequest.getRentArea().split(","))
                    .map(String::trim)
                    .filter(i -> !i.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            buildingEntityNew.setRentAreaValues(rentAreaValues);
        } else {
            buildingEntityNew.setRentAreaValues(Collections.emptyList());
        }
        return buildingEntityNew;
    }

    public BuildingSearchResponse toBuildingSearchResponse(BuildingEntity buildingEntity) {
        BuildingSearchResponse buildingResponse = modelMapper.map(buildingEntity, BuildingSearchResponse.class);
        StringBuilder addressBuilder = new StringBuilder();
        if ((buildingEntity.getStreet() != null && !buildingEntity.getStreet().isEmpty())) {
            addressBuilder.append(buildingEntity.getStreet());
            if ((buildingEntity.getWard() != null && !buildingEntity.getWard().isEmpty()) || (buildingEntity.getDistrict() != null && !buildingEntity.getDistrict().isEmpty())) {
                addressBuilder.append(", ");
            }
        }
        if (buildingEntity.getWard() != null && !buildingEntity.getWard().isEmpty()) {
            addressBuilder.append(buildingEntity.getWard());
            if (buildingEntity.getDistrict() != null && !buildingEntity.getDistrict().isEmpty()) {
                addressBuilder.append(", ");
            }

        }
        if (buildingEntity.getDistrict() != null && !buildingEntity.getDistrict().isEmpty()) {
            try {
                District districtEnum = District.valueOf(buildingEntity.getDistrict());
                addressBuilder.append(districtEnum.getName());
            } catch (IllegalArgumentException e) {
                addressBuilder.append(buildingEntity.getDistrict());
            }
        }
        buildingResponse.setAddress(addressBuilder.toString());
        List<Long> rentAreaValues = buildingEntity.getRentAreaValues() == null ? Collections.emptyList() : buildingEntity.getRentAreaValues();
        String rentArea = rentAreaValues.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        buildingResponse.setRentArea(rentArea);
        buildingResponse.setType(buildingEntity.getType());
        buildingResponse.setNote(buildingEntity.getNote());
        buildingResponse.setAvatar(buildingEntity.getAvatar());
        if (buildingEntity.getSaleStatus() == null || buildingEntity.getSaleStatus().isBlank()) {
            buildingResponse.setSaleStatus(BuildingSaleStatus.FOR_SALE.name());
        }
        return buildingResponse;
    }

    public BuildingRequest toBuildingRequest(BuildingEntity buildingEntity) {
        BuildingRequest buildingRequest = modelMapper.map(buildingEntity, BuildingRequest.class);
        if (buildingEntity.getSaleStatus() == null || buildingEntity.getSaleStatus().isBlank()) {
            buildingRequest.setSaleStatus(BuildingSaleStatus.FOR_SALE.name());
        }
        if (buildingEntity.getTypeCode() != null) {
            buildingRequest.setTypeCode(buildingEntity.getTypeCode());
        } else if (buildingEntity.getType() != null && !buildingEntity.getType().isBlank()) {
            try {
                buildingRequest.setTypeCode(BuildingTypeCode.valueOf(buildingEntity.getType().trim()));
            } catch (IllegalArgumentException ignored) {
                buildingRequest.setTypeCode(BuildingTypeCode.CAN_HO);
            }
        } else {
            buildingRequest.setTypeCode(BuildingTypeCode.CAN_HO);
        }
        List<Long> rentAreaValues = buildingEntity.getRentAreaValues() == null ? Collections.emptyList() : buildingEntity.getRentAreaValues();
        String rentArea = rentAreaValues.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        buildingRequest.setRentArea(rentArea);
        return buildingRequest;
    }
}
