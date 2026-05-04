package com.example.bedatn.repository;

import com.example.bedatn.documents.RentAreaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RentAreaRepository extends MongoRepository<RentAreaEntity, Long> {
    List<RentAreaEntity> findByBuildingIdIn(List<Long> ids);
    List<RentAreaEntity> findByBuildingId(Long buildingId);


}
