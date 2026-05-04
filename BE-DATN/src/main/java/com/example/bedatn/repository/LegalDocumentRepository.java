package com.example.bedatn.repository;

import com.example.bedatn.documents.LegalDocumentEntity;
import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.enums.DocType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface LegalDocumentRepository extends MongoRepository<LegalDocumentEntity, String> {
    List<LegalDocumentEntity> findByBuildingIdOrderByCreatedAtDesc(String buildingId);

    List<LegalDocumentEntity> findByStatusAndExpireDateBetweenOrderByExpireDateAsc(DocStatus status, LocalDate from, LocalDate to);

    boolean existsByBuildingIdAndDocTypeAndStatus(String buildingId, DocType docType, DocStatus status);

    LegalDocumentEntity findFirstByBuildingIdAndDocTypeAndStatusOrderByCreatedAtDesc(String buildingId, DocType docType, DocStatus status);
}
