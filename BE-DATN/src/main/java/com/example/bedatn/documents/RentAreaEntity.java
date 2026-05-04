package com.example.bedatn.documents;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rentarea")
@Getter
@Setter
public class RentAreaEntity extends BaseEntity{

    private Long value;

    private Long buildingId;

}
