package com.example.bedatn.service.impl;

import com.example.bedatn.documents.AssignmentCustomerEntity;
import com.example.bedatn.dto.request.AssignmentCustomerRequest;
import com.example.bedatn.repository.AssignmentCustomerRepository;
import com.example.bedatn.service.AssignmentCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class AssignmentCustomerServiceImpl implements AssignmentCustomerService {

    @Autowired
    private AssignmentCustomerRepository assignmentCustomerRepository;

    @Override
    public String updateAssignment(AssignmentCustomerRequest request) {
        assignmentCustomerRepository.deleteByCustomerId(request.getCustomerId());
        List<Long> assigns = request.getStaffs() == null ? Collections.emptyList() : request.getStaffs();
        for (Long assign : assigns) {
            AssignmentCustomerEntity assignmentCustomerEntity = new AssignmentCustomerEntity();
            assignmentCustomerEntity.setId(System.nanoTime());
            assignmentCustomerEntity.setCustomerId(request.getCustomerId());
            assignmentCustomerEntity.setStaffId(assign);
            assignmentCustomerRepository.save(assignmentCustomerEntity);
        }
        return "Success";
    }
}
