package com.example.bedatn.documents;

import com.example.bedatn.enums.BuildingTypeCode;
import com.example.bedatn.enums.CardinalDirection;
import com.example.bedatn.enums.FurnitureType;
import com.example.bedatn.enums.LandType;
import com.example.bedatn.enums.LegalStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "buildings")
@Getter
@Setter
public class BuildingEntity extends BaseEntity{

    private String name;

    private String street;

    private String ward;

    private String district;

    private String province;

    // Stored as list in Mongo instead of joined rentarea table.
    private List<Long> rentAreaValues;

    private Double area;

    private BigDecimal price;

    private String structure;

    private Integer numberOfBasement;

    private Double floorArea;

    private CardinalDirection direction;

    private LegalStatus legalStatus;

    private Integer level;

    private Long rentPrice;

    private String rentPriceDescription;

    private BigDecimal serviceFee;

    private String carFee;

    private String motoFee;

    private String overTimeFee;

    private String waterFee;

    private String electricityFee;

    private String deposit;

    private String payment;

    private String rentTime;

    private String decorationTime;

    private Float brokerageFee;

    private String note;
    private String type;

    private BuildingTypeCode typeCode;

    private String linkOfBuilding;

    private String map;

    private String avatar;

    /** Danh sách đường dẫn ảnh phụ (gallery) */
    private List<String> images;

    /** Danh sách ID hồ sơ pháp lý đã upload cho BĐS này */
    private List<String> legalDocumentIds;

    private String managerName;

    private String managerPhone;

    /** {@link com.example.bedatn.enums.BuildingSaleStatus} — lưu dạng chuỗi enum name */
    private String saleStatus;

    /** Khách hàng gắn với đặt cọc / đã bán (bắt buộc khi không phải FOR_SALE) */
    private Long linkedCustomerId;

    private String customerId;

    private String buildingName;

    private Integer bedrooms;

    private Integer bathrooms;

    private FurnitureType furniture;

    private Double frontage;

    private LandType landType;

    private Double width;

    private Double length;

    private String roadWidth;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
