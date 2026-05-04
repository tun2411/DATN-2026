package com.example.bedatn.service.impl;

import com.example.bedatn.converter.CustomerConverter;
import com.example.bedatn.documents.*;
import com.example.bedatn.dto.request.CustomerRequest;
import com.example.bedatn.dto.request.CustomerSearchRequest;
import com.example.bedatn.dto.response.CustomerOptionResponse;
import com.example.bedatn.dto.response.CustomerSearchResponse;
import com.example.bedatn.dto.response.StaffResponse;
import com.example.bedatn.enums.Status;
import com.example.bedatn.repository.AssignmentCustomerRepository;
import com.example.bedatn.repository.CustomerRepository;
import com.example.bedatn.repository.TransactionRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerConverter customerConverter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AssignmentCustomerRepository assignmentCustomerRepository;

    @Autowired
    private ContactNotificationPublisher contactNotificationPublisher;

    @Override
    public boolean existsByPhone(String phone, Long id) {
        return customerRepository.existsByPhoneAndActiveAndIdNot(phone, 1L, id != null ? id : -1L);
    }

    @Override
    public boolean existsByEmail(String email, Long id) {
        return customerRepository.existsByEmailAndActiveAndIdNot(email, 1L, id != null ? id : -1L);
    }

    @Override
    public List<CustomerSearchResponse> searchCustomers(CustomerSearchRequest customerSearchRequest, Pageable pageable) {
        customerSearchRequest.setIs_Active(1L);
        List<CustomerEntity> customerEntities = customerRepository.searchCustomers(customerSearchRequest, pageable);
        List<CustomerSearchResponse> results = new ArrayList<>();
        for (CustomerEntity customerEntity : customerEntities) {
            CustomerSearchResponse customerResponse = customerConverter.toCustomerSearchResponse(customerEntity);
            results.add(customerResponse);
        }
        return results;
    }

    @Override
    public CustomerRequest findCustomerById(Long id) {
        CustomerEntity customerEntity = customerRepository.findById(id).get();
        return customerConverter.toCustomerRequest(customerEntity);
    }

    @Override
    public CustomerEntity createCustomer(CustomerRequest request) {
        if (existsByPhone(request.getPhone(), null)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống.");
        }
        if (existsByEmail(request.getEmail(), null)) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống.");
        }
        request.setIs_Active(1L);
        CustomerEntity customerEntity = customerConverter.toEntity(request);
        if (customerEntity.getId() == null) {
            customerEntity.setId(System.currentTimeMillis());
        }
        if (customerEntity.getCreatedDate() == null) {
            customerEntity.setCreatedDate(new Date());
        }
        customerRepository.save(customerEntity);
        return customerEntity;
    }

    @Override
    public void contactUser(CustomerRequest request) {
        CustomerEntity customerEntity = customerConverter.toEntity(request);
        if (customerEntity.getId() == null) {
            customerEntity.setId(System.currentTimeMillis());
        }
        if (customerEntity.getCreatedDate() == null) {
            customerEntity.setCreatedDate(new Date());
        }
        customerEntity.setStatus(Status.getNameByStatus(Status.CHUA_XU_LY));
        customerEntity.setActive(1L);
        customerRepository.save(customerEntity);
        contactNotificationPublisher.publishContactLead(customerEntity);
    }

    @Override
    public CustomerEntity updateCustomer(CustomerRequest request) {
        if (existsByPhone(request.getPhone(), request.getId())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống.");
        }
        if (existsByEmail(request.getEmail(), request.getId())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống.");
        }
        request.setIs_Active(1L);
        CustomerEntity customer = customerRepository.findById(request.getId()).get();
        Date createdDate = customer.getCreatedDate();
        String createdBy = customer.getCreatedBy();
        CustomerEntity customerEntity = customerConverter.toEntity(request);
        customerEntity.setId(customer.getId());
        customerEntity.setCreatedDate(createdDate);
        customerEntity.setCreatedBy(createdBy);
        customerRepository.save(customerEntity);
        return customerEntity;
    }

    @Override
    public String delete(List<Long> ids) {
        List<AssignmentCustomerEntity> assignments = assignmentCustomerRepository.findByCustomerIdIn(ids);
        assignmentCustomerRepository.deleteAll(assignments);
        for (Long id : ids) {
            CustomerEntity customerEntity = customerRepository.findById(id).get();
            customerEntity.setActive(0L);
            customerRepository.save(customerEntity);
            transactionRepository.deleteByCustomerId(id);
        }
        return "Success";
    }

    @Override
    public List<StaffResponse> findAssignedStaffs(Long id) {
        List<UserEntity> staffList = userRepository.findByStatusAndRoleCode(1, "STAFF");
        List<UserEntity> assignedStaffs = userService.getUsersByCustomerId(id);
        List<StaffResponse> staffResponseDTOS = new ArrayList<>();
        for (UserEntity staff : staffList) {
            StaffResponse sr = new StaffResponse();
            sr.setStaffId(staff.getId());
            sr.setFullName(staff.getFullName());
            if (assignedStaffs.contains(staff)) {
                sr.setChecked("checked");
            } else {
                sr.setChecked("");
            }
            staffResponseDTOS.add(sr);
        }
        return staffResponseDTOS;
    }

    @Override
    public int countTotalItems(CustomerSearchRequest customerSearchRequest) {
        return customerRepository.countTotalItem(customerSearchRequest);
    }

    @Override
    public CustomerEntity findById(Long id) {
        return customerRepository.findById(id).get();
    }

    @Override
    public boolean checkAssignedStaff(Long customerId, Long staffId) {
        if (customerId == null || staffId == null) {
            throw new IllegalArgumentException("customerId and staffId must not be null");
        }
        List<AssignmentCustomerEntity> assignments = assignmentCustomerRepository.findByCustomerId(customerId);
        if (assignments == null || assignments.isEmpty()) {
            return false;
        }
        return assignments.stream().anyMatch(assignment -> Objects.equals(assignment.getStaffId(), staffId));
    }

    @Override
    public List<CustomerOptionResponse> listActiveOptions(Pageable pageable) {
        return customerRepository.findByActive(1L, pageable).getContent().stream()
                .map(c -> {
                    CustomerOptionResponse o = new CustomerOptionResponse();
                    o.setId(c.getId());
                    o.setFullName(c.getFullName());
                    o.setPhone(c.getPhone());
                    return o;
                })
                .collect(Collectors.toList());
    }
}
