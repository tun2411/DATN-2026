package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = -863164858986274318L;

    @Id
    private Long id;

    @CreatedDate
    private Date createdDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private Date modifiedDate;

    @LastModifiedBy
    private String modifiedBy;

}
