package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class AbstractResponse<T> implements Serializable {

    private static final long serialVersionUID = 7213600440729202783L;

    private Long id;
    private Date createdDate;
    private String createdBy;
    private Date modifiedDate;
    private String modifiedBy;
    private int maxPageItems = 2;
    private int page = 1;
    private List<T> listResult = new ArrayList<>();
    private int totalItems = 0;
    private String tableId = "tableList";
    private Integer limit;
    private Integer totalPage;
    private Integer totalItem;
    private String searchValue;
}
