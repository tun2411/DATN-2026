package com.example.bedatn.repository;

import com.example.bedatn.documents.AssignmentBuildingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssignmentBuildingRepository extends MongoRepository<AssignmentBuildingEntity,Long> {

    void deleteByBuildingId(Long id);
    List<AssignmentBuildingEntity> findByBuildingId(Long buildingId);
    List<AssignmentBuildingEntity> findByBuildingIdIn(List<Long> ids);
    List<AssignmentBuildingEntity> findByStaffId(Long staffId);

}
