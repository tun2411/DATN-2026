package com.example.bedatn.repository.custom.impl;

import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.documents.AssignmentCustomerEntity;
import com.example.bedatn.dto.request.CustomerSearchRequest;
import com.example.bedatn.repository.AssignmentCustomerRepository;
import com.example.bedatn.repository.custom.CustomerRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AssignmentCustomerRepository assignmentCustomerRepository;

    @Override
    public List<CustomerEntity> searchCustomers(CustomerSearchRequest customerSearchRequest, Pageable pageable) {
        Query query = buildQuery(customerSearchRequest);
        query.with(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdDate")));
        return mongoTemplate.find(query, CustomerEntity.class);
    }

    @Override
    public int countTotalItem(CustomerSearchRequest request) {
        return (int) mongoTemplate.count(buildQuery(request), CustomerEntity.class);
    }

    private Query buildQuery(CustomerSearchRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();
        addRegex(criteriaList, "fullName", request.getFullName());
        addRegex(criteriaList, "phone", request.getPhone());
        addRegex(criteriaList, "email", request.getEmail());
        addRegex(criteriaList, "status", request.getStatus());
        if (request.getIs_Active() != null) {
            criteriaList.add(Criteria.where("is_Active").is(request.getIs_Active()));
        }
        if (request.getStaffId() != null) {
            List<Long> customerIds = assignmentCustomerRepository.findAll().stream()
                    .filter(item -> request.getStaffId().equals(item.getStaffId()))
                    .map(AssignmentCustomerEntity::getCustomerId)
                    .collect(Collectors.toList());
            criteriaList.add(Criteria.where("id").in(customerIds));
        }
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private void addRegex(List<Criteria> criteriaList, String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            criteriaList.add(Criteria.where(field).regex(value, "i"));
        }
    }
}
