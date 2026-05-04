package com.example.bedatn.repository.custom;

import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.dto.request.CustomerSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerRepositoryCustom {
    List<CustomerEntity> searchCustomers(CustomerSearchRequest customerSearchRequest, Pageable pageable);
    int countTotalItem(CustomerSearchRequest request);
}
