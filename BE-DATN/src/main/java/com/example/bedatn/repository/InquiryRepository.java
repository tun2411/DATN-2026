package com.example.bedatn.repository;

import com.example.bedatn.documents.InquiryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InquiryRepository extends MongoRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByStatusOrderByCreatedDateDesc(String status);
    List<InquiryEntity> findByStatusInOrderByCreatedDateDesc(List<String> statuses);
    List<InquiryEntity> findByAssignedStaffIdOrderByCreatedDateDesc(Long staffId);
}
