package com.example.bedatn.scheduler;

import com.example.bedatn.documents.LegalDocumentEntity;
import com.example.bedatn.enums.DocStatus;
import com.example.bedatn.repository.LegalDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class LegalExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(LegalExpiryScheduler.class);

    private final LegalDocumentRepository legalDocumentRepository;
    private final MongoTemplate mongoTemplate;

    public LegalExpiryScheduler(LegalDocumentRepository legalDocumentRepository, MongoTemplate mongoTemplate) {
        this.legalDocumentRepository = legalDocumentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void checkLegalDocumentExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate warningUntil = today.plusDays(30);

        legalDocumentRepository.findByStatusAndExpireDateBetweenOrderByExpireDateAsc(DocStatus.VERIFIED, today, warningUntil)
                .forEach(doc -> log.warn("[LEGAL WARNING] BĐS {} - {} hết hạn vào {}",
                        doc.getBuildingId(), doc.getDocType(), doc.getExpireDate()));

        Query expiredQuery = new Query(new Criteria().andOperator(
                Criteria.where("status").is(DocStatus.VERIFIED),
                Criteria.where("expireDate").lt(today)
        ));
        Update update = new Update()
                .set("status", DocStatus.EXPIRED)
                .set("updatedAt", LocalDateTime.now());
        mongoTemplate.updateMulti(expiredQuery, update, LegalDocumentEntity.class);
    }
}
