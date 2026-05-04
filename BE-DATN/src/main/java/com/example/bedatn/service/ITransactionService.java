package com.example.bedatn.service;

import com.example.bedatn.dto.request.TransactionRequest;
import com.example.bedatn.documents.TransactionEntity;

import java.util.List;

public interface ITransactionService {
    TransactionEntity saveTransaction(TransactionRequest transactionRequest);
    TransactionEntity updateTransaction(Long id, TransactionRequest transactionRequest);
    void deleteTransaction(Long id);
    TransactionEntity findById(Long id);
    List<TransactionEntity> findAllTransactions();
    List<TransactionEntity> findByCustomerId(Long customerId);
    List<TransactionEntity> findByCustomerIds(List<Long> customerIds);
    List<TransactionEntity> findByStaffId(Long staffId);
    List<TransactionEntity> getTransactionByCodeAndCustomerId(String code, Long customerId);
    List<com.example.bedatn.dto.response.TransactionProgressResponse> getProgressByCustomerId(Long customerId);
}

