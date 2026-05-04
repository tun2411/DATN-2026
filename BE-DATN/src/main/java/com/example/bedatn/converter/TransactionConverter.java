package com.example.bedatn.converter;

import com.example.bedatn.dto.request.TransactionRequest;
import com.example.bedatn.dto.response.TransactionResponse;
import com.example.bedatn.documents.TransactionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter {

    @Autowired
    private ModelMapper modelMapper;

    public TransactionResponse toResponse(TransactionEntity transaction) {
        return modelMapper.map(transaction, TransactionResponse.class);
    }

    public TransactionEntity toEntity(TransactionRequest dto) {
        return modelMapper.map(dto, TransactionEntity.class);
    }
}
