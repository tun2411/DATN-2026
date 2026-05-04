package com.example.bedatn.dto.request;

import com.example.bedatn.enums.BuildingTypeCode;
import com.example.bedatn.enums.CardinalDirection;
import com.example.bedatn.enums.FurnitureType;
import com.example.bedatn.enums.LandType;
import com.example.bedatn.enums.LegalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class BuildingRequest extends AbstractRequest<Object> {
    @NotBlank(message = "Building name not be blank!")
    private String name;
    private String street;
    private String ward;
    @NotBlank(message = "District not be blank!")
    private String district;
    private String province;
    private Integer numberOfBasement;
    private Double floorArea;
    private Integer level;
    @NotNull(message = "Building type is required")
    private BuildingTypeCode typeCode;
    private String overTimeFee;
    private String electricityFee;
    private String deposit;
    private String payment;
    private String rentTime;
    private String decorationTime;
    private String rentPriceDescription;
    private String carFee;
    private String motoFee;
    private String waterFee;
    private String structure;
    private CardinalDirection direction;
    private LegalStatus legalStatus;
    private String note;
    private String rentArea;
    private String managerName;
    private String managerPhone;
    private Long rentPrice;
    @NotNull(message = "price not be null")
    private BigDecimal price;
    @NotNull(message = "area not be null")
    private Double area;
    private BigDecimal serviceFee;
    private Float brokerageFee;
    private String avatar;
    private String imageBase64;
    private String imageName;

    /** Danh sách đường dẫn ảnh phụ (gallery) đã lưu trên server */
    private List<String> images;
    /** Base64 của các ảnh mới cần upload (tương ứng theo index với imageNames) */
    private List<String> imagesBase64;
    /** Tên file tương ứng */
    private List<String> imageNames;

    /** FOR_SALE | DEPOSIT | SOLD */
    private String saleStatus;

    /** Bắt buộc khi saleStatus là DEPOSIT hoặc SOLD */
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

    private Map<String, String> buildingDTOs = new HashMap<>();

    public String getImageBase64() {
        if (imageBase64 != null) {
            return imageBase64.split(",")[1];
        }
        return null;
    }
}
