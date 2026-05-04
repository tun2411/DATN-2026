package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerListPageResponse {
    private List<CustomerSearchResponse> items;
    private int totalItems;
    private int page;
    private int pageSize;
    private int totalPages;
}
