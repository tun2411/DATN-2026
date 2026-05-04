package com.example.bedatn.repository;

import com.example.bedatn.documents.SupportChatSessionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SupportChatSessionRepository extends MongoRepository<SupportChatSessionEntity, Long> {

    Optional<SupportChatSessionEntity> findByVisitorKey(String visitorKey);

    List<SupportChatSessionEntity> findByStatusInOrderByCreatedDateDesc(List<String> statuses);

    Optional<SupportChatSessionEntity> findByUserIdAndBuildingId(Long userId, Long buildingId);

    List<SupportChatSessionEntity> findByUserIdOrderByCreatedDateDesc(Long userId);

    /** Session "general" của user: không gắn buildingId */
    Optional<SupportChatSessionEntity> findByUserIdAndBuildingIdIsNull(Long userId);
}
