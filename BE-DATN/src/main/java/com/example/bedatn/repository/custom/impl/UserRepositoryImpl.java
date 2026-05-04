package com.example.bedatn.repository.custom.impl;

import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.repository.custom.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<UserEntity> findByRole(String roleCode) {
		Query query = new Query();
		query.addCriteria(Criteria.where("roleCode").is(roleCode));
		return mongoTemplate.find(query, UserEntity.class);
	}

	@Override
	public List<UserEntity> getAllUsers(Pageable pageable) {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is(1));
		query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
		query.skip(pageable.getOffset());
		query.limit(pageable.getPageSize());
		return mongoTemplate.find(query, UserEntity.class);
	}

	@Override
	public int countTotalItem() {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is(1));
		return (int) mongoTemplate.count(query, UserEntity.class);
	}
}
