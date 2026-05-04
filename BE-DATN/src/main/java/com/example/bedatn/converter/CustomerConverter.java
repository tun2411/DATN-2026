package com.example.bedatn.converter;

import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.dto.request.CustomerRequest;
import com.example.bedatn.dto.response.CustomerSearchResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerConverter {
    @Autowired
    private ModelMapper modelMapper;

    public CustomerSearchResponse toCustomerSearchResponse(CustomerEntity customerEntity) {
        return modelMapper.map(customerEntity, CustomerSearchResponse.class);
    }

    public CustomerEntity toEntity(CustomerRequest request) {
        CustomerEntity entity = modelMapper.map(request, CustomerEntity.class);
        entity.setActive(request.getIs_Active());
        return entity;
    }

    public CustomerRequest toCustomerRequest(CustomerEntity customerEntity) {
        CustomerRequest request = modelMapper.map(customerEntity, CustomerRequest.class);
        request.setIs_Active(customerEntity.getActive());
        return request;
    }
}
