package com.example.bedatn.repository.custom;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.dto.request.BuildingSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuildingRepositoryCustom {
    List<BuildingEntity> searchBuildings(BuildingSearchRequest buildingSearchRequest, Pageable pageable);
    int countTotalItem(BuildingSearchRequest request);
}