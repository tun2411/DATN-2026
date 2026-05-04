package com.example.bedatn.repository;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.repository.custom.BuildingRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BuildingRepository extends MongoRepository<BuildingEntity, Long>, BuildingRepositoryCustom {
    List<BuildingEntity> findByNameContaining(String keySearch);
    void deleteAllByIdIn(List<Long> ids);
    List<BuildingEntity> findByIdIn(List<Long> ids);
    List<BuildingEntity> findByLinkedCustomerId(Long linkedCustomerId);
    List<BuildingEntity> findBySaleStatus(String saleStatus);



}
