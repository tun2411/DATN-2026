package com.example.bedatn.enums;

import java.util.Map;
import java.util.TreeMap;

public enum Transaction {
    CSKH("Chăm sóc khách hàng"),
    DDX("Dẫn đi xem");


    private final String transactionName;

    Transaction(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getTransaction() {
        return transactionName;
    }

    public static Map<String,String> getTransactionType(){
        Map<String,String> transactions = new TreeMap<>();
        for(Transaction transaction:Transaction.values()){
            transactions.put(transaction.toString(),transaction.transactionName);
        }
        return transactions;
    }

}
