package com.example.bedatn.repository;

import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.repository.custom.CustomerRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomerRepository extends MongoRepository<CustomerEntity, Long>, CustomerRepositoryCustom {
    void deleteAllByIdIn(List<Long> ids);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhoneAndActiveAndIdNot(String phone, Long active, Long id);
    boolean existsByEmailAndActiveAndIdNot(String email, Long active, Long id);
    CustomerEntity findFirstByEmailAndActive(String email, Long active);

    Page<CustomerEntity> findByActive(Long active, Pageable pageable);

}
