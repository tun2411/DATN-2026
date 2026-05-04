package com.example.bedatn.repository;


import com.example.bedatn.documents.TransactionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<TransactionEntity, Long> {
    // Tìm tất cả giao dịch theo customerId
    List<TransactionEntity> findByCustomerId(Long customerId);
    List<TransactionEntity> findByCustomerIdIn(List<Long> customerIds);

    // Tìm tất cả giao dịch theo staffId
    List<TransactionEntity> findByStaffId(Long staffId);

    List<TransactionEntity> findByCodeAndCustomerId(String code, Long customerId);

    List<TransactionEntity> findByCustomerIdAndBuildingId(Long customerId, Long buildingId);

    void deleteByCustomerId(Long id);

    List<TransactionEntity> findTop5ByOrderByCreatedDateDesc();

    List<TransactionEntity> findTop5ByCustomerIdInOrderByCreatedDateDesc(List<Long> customerIds);

    List<TransactionEntity> findAllByCustomerIdIn(List<Long> customerIds, Sort sort);
}
