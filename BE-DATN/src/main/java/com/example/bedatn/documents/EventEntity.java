package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "event")
@Getter
@Setter
public class EventEntity extends BaseEntity {

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
