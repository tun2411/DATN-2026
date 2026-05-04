package com.example.bedatn.repository;

import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.documents.UserRoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRoleRepository extends MongoRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
