package com.example.bedatn.repository;

import com.example.bedatn.documents.EventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventRepository extends MongoRepository<EventEntity, Long> {

    List<EventEntity> findByStatusOrderByStartDateDesc(String status);
}
