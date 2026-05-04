package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EventResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String location;
    private Date startDate;
    private Date endDate;
    private String status;
}
