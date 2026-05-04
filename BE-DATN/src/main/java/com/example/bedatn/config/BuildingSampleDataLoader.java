package com.example.bedatn.config;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.enums.BuildingSaleStatus;
import com.example.bedatn.enums.BuildingTypeCode;
import com.example.bedatn.repository.BuildingRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Chèn vài bản ghi BĐS mẫu khi collection {@code building} đang trống.
 * Bật/tắt bằng {@code app.seed-buildings=true} trong application.properties.
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "app.seed-buildings", havingValue = "true")
public class BuildingSampleDataLoader implements ApplicationRunner {

    private final BuildingRepository buildingRepository;

    public BuildingSampleDataLoader(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (buildingRepository.count() > 0) {
            return;
        }
        long t = System.currentTimeMillis();
        buildingRepository.saveAll(List.of(
                sample(t + 1, "Masteri Centre Point", "QUAN_2",
                        "Nguyễn Văn Linh", "Bình An",
                        "NGUYEN_CAN", 78L, 4_200_000_000L,
                        "Thiết kế hiện đại, kết nối thuận tiện KĐT Thủ Thiêm."),
                sample(t + 2, "Vinhomes Riverside Villa", "QUAN_3",
                        "Đường số 1", "Phường 3",
                        "NGUYEN_CAN", 200L, 15_500_000_000L,
                        "Biệt thự sinh thái ven sông, tiện ích đầy đủ."),
                sample(t + 3, "Sun Grand City Penthouse", "QUAN_1",
                        "Phó Đức Chính", "Phường Bến Nghé",
                        "NOI_THAT", 300L, 32_000_000_000L,
                        "Penthouse view trung tâm, không gian rộng rãi."),
                sample(t + 4, "Eco Green Townhouse", "QUAN_5",
                        "Nguyễn Văn Linh", "Phường 5",
                        "TANG_TRET", 110L, 8_500_000_000L,
                        "Nhà phố thương mại, mặt tiền kinh doanh tốt.")
        ));
    }

    private static BuildingEntity sample(
            long id,
            String name,
            String district,
            String street,
            String ward,
            String type,
            long floorArea,
            long rentPrice,
            String note) {
        BuildingEntity e = new BuildingEntity();
        e.setId(id);
        e.setName(name);
        e.setDistrict(district);
        e.setStreet(street);
        e.setWard(ward);
        e.setType(type);
        e.setTypeCode(BuildingTypeCode.valueOf(type));
        e.setFloorArea((double) floorArea);
        e.setRentPrice(rentPrice);
        e.setRentAreaValues(List.of(floorArea));
        e.setNumberOfBasement(1);
        e.setNote(note);
        e.setSaleStatus(BuildingSaleStatus.FOR_SALE.name());
        e.setCreatedDate(new Date());
        return e;
    }
}
