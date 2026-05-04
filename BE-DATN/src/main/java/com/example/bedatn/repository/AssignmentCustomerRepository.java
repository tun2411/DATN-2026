package com.example.bedatn.repository;

import com.example.bedatn.documents.AssignmentCustomerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssignmentCustomerRepository extends MongoRepository<AssignmentCustomerEntity,Long> {

    void deleteByCustomerId(Long id);
    List<AssignmentCustomerEntity> findByCustomerId(Long buildingId);
    List<AssignmentCustomerEntity> findByCustomerIdIn(List<Long> ids);


}
