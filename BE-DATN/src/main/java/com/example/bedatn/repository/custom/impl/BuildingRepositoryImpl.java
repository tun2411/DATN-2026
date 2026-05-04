package com.example.bedatn.repository.custom.impl;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.RentAreaEntity;
import com.example.bedatn.documents.AssignmentBuildingEntity;
import com.example.bedatn.dto.request.BuildingSearchRequest;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.RentAreaRepository;
import com.example.bedatn.repository.custom.BuildingRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class BuildingRepositoryImpl implements BuildingRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RentAreaRepository rentAreaRepository;

    @Autowired
    private AssignmentBuildingRepository assignmentBuildingRepository;

    @Override
    public List<BuildingEntity> searchBuildings(BuildingSearchRequest buildingSearchRequest, Pageable pageable) {
        Query query = buildQuery(buildingSearchRequest);
        query.with(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdDate")));
        return mongoTemplate.find(query, BuildingEntity.class);
    }

    @Override
    public int countTotalItem(BuildingSearchRequest request) {
        Query query = buildQuery(request);
        return (int) mongoTemplate.count(query, BuildingEntity.class);
    }

    private Query buildQuery(BuildingSearchRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();
        addRegex(criteriaList, "name", request.getName());
        addEquals(criteriaList, "floorArea", request.getFloorArea());
        addRegex(criteriaList, "district", request.getDistrict());
        addRegex(criteriaList, "ward", request.getWard());
        addRegex(criteriaList, "street", request.getStreet());
        addEquals(criteriaList, "numberOfBasement", request.getNumberOfBasement());
        addRegex(criteriaList, "direction", request.getDirection());
        addRegex(criteriaList, "level", request.getLevel());
        addRegex(criteriaList, "managerName", request.getManagerName());
        addRegex(criteriaList, "managerPhone", request.getManagerPhone());

        if (request.getRentPriceFrom() != null || request.getRentPriceTo() != null) {
            Criteria rentPriceCriteria = Criteria.where("rentPrice");
            if (request.getRentPriceFrom() != null) {
                rentPriceCriteria.gte(request.getRentPriceFrom());
            }
            if (request.getRentPriceTo() != null) {
                rentPriceCriteria.lte(request.getRentPriceTo());
            }
            criteriaList.add(rentPriceCriteria);
        }

        if (request.getTypeCode() != null && !request.getTypeCode().isEmpty()) {
            List<Criteria> typeCriterias = new ArrayList<>();
            for (String typeCode : request.getTypeCode()) {
                typeCriterias.add(Criteria.where("type").regex(typeCode, "i"));
            }
            criteriaList.add(new Criteria().orOperator(typeCriterias.toArray(new Criteria[0])));
        }

        Set<Long> filteredIds = null;
        if (request.getStaffId() != null) {
            List<AssignmentBuildingEntity> assignments = assignmentBuildingRepository.findAll().stream()
                    .filter(item -> request.getStaffId().equals(item.getStaffId()))
                    .collect(Collectors.toList());
            filteredIds = assignments.stream().map(AssignmentBuildingEntity::getBuildingId).collect(Collectors.toSet());
        }
        if (request.getRentAreaFrom() != null || request.getRentAreaTo() != null) {
            List<RentAreaEntity> rentAreas = rentAreaRepository.findAll();
            Set<Long> rentIds = rentAreas.stream()
                    .filter(item -> request.getRentAreaFrom() == null || item.getValue() >= request.getRentAreaFrom())
                    .filter(item -> request.getRentAreaTo() == null || item.getValue() <= request.getRentAreaTo())
                    .map(RentAreaEntity::getBuildingId)
                    .collect(Collectors.toSet());
            filteredIds = filteredIds == null ? rentIds : intersection(filteredIds, rentIds);
        }
        if (filteredIds != null) {
            criteriaList.add(Criteria.where("id").in(filteredIds));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private Set<Long> intersection(Set<Long> first, Set<Long> second) {
        Set<Long> result = new HashSet<>(first);
        result.retainAll(second);
        return result;
    }

    private void addRegex(List<Criteria> criteriaList, String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            criteriaList.add(Criteria.where(field).regex(value, "i"));
        }
    }

    private void addEquals(List<Criteria> criteriaList, String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).is(value));
        }
    }
}