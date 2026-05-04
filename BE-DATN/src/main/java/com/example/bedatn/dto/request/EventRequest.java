package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EventRequest {
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String location;
    private Date startDate;
    private Date endDate;
    /** DRAFT | PUBLISHED */
    private String status;
}
