package com.example.bedatn.repository;

import com.example.bedatn.documents.RoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<RoleEntity,Long> {
	RoleEntity findOneByCode(String code);
}
