package com.example.bedatn.service;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.dto.request.BuildingRequest;
import com.example.bedatn.dto.request.BuildingSearchRequest;
import com.example.bedatn.dto.response.BuildingSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuildingService {

    List<BuildingSearchResponse> searchBuildings(BuildingSearchRequest buildingSearchRequest, Pageable pageable);

    BuildingRequest findBuildingById(Long id);
    String delete(List<Long> ids);
    List<StaffResponse> findAssignedStaffs(Long id);
    List<StaffResponse> findAllStaffs();
    BuildingEntity createBuilding(BuildingRequest buildingRequest);
    BuildingEntity updateBuilding(BuildingRequest buildingRequest);
    boolean checkAssignedStaff(Long buildingId, Long staffId);
    int countTotalItems(BuildingSearchRequest buildingSearchRequest);
    void saveThumbnail(BuildingRequest buildingRequest, BuildingEntity buildingEntity);
}