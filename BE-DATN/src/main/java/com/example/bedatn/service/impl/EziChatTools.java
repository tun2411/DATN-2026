package com.example.bedatn.service.impl;

import com.example.bedatn.documents.BuildingEntity;
import com.example.bedatn.documents.SupportChatSessionEntity;
import com.example.bedatn.repository.SupportChatSessionRepository;
import com.example.bedatn.supportchat.BuildingFilterNormalizer;
import com.example.bedatn.supportchat.BuildingSearchFilter;
import com.example.bedatn.supportchat.SupportChatContextHolder;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Công cụ gọi từ Spring AI (Gemini) — truy vấn BĐS, lưu lead, chuyển nhân viên.
 */
@Component
public class EziChatTools {

    private static final int MAX_BUILDINGS = 5;

    private final MongoTemplate mongoTemplate;
    private final BuildingFilterNormalizer normalizer;
    private final SupportChatSessionRepository sessionRepository;
    private final SupportChatHandoverPublisher handoverPublisher;

    public EziChatTools(MongoTemplate mongoTemplate,
                        BuildingFilterNormalizer normalizer,
                        SupportChatSessionRepository sessionRepository,
                        SupportChatHandoverPublisher handoverPublisher) {
        this.mongoTemplate = mongoTemplate;
        this.normalizer = normalizer;
        this.sessionRepository = sessionRepository;
        this.handoverPublisher = handoverPublisher;
    }

    @Tool(description = """
            Tìm kiếm bất động sản theo tiêu chí của khách.
            Truyền các tiêu chí khách đề cập, bỏ trống những gì khách chưa nói.
            Nếu khách cung cấp tên BĐS/dự án, truyền vào name để lấy thông tin cụ thể.
            Ví dụ: name='The Vista', typeCode='căn hộ', maxPrice=3000000000, district='Quận 9', bedrooms=2
            """)
    public String searchProperties(
            @ToolParam(description = "Tên hoặc một phần tên BĐS/dự án khách nhắc tới")
            String name,
            @ToolParam(description = "Loại BĐS: căn hộ | nhà | đất (hoặc CAN_HO | NGUYEN_CAN | DAT_NEN)")
            String typeCode,
            @ToolParam(description = "Quận/huyện, ví dụ: Quận 9, Bình Thạnh")
            String district,
            @ToolParam(description = "Phường/xã")
            String ward,
            @ToolParam(description = "Giá tối thiểu (đồng), ví dụ: 1000000000")
            Long minPrice,
            @ToolParam(description = "Giá tối đa (đồng), ví dụ: 3000000000")
            Long maxPrice,
            @ToolParam(description = "Diện tích tối thiểu (m²)")
            Double minArea,
            @ToolParam(description = "Diện tích tối đa (m²)")
            Double maxArea,
            @ToolParam(description = "Số phòng ngủ")
            Integer bedrooms,
            @ToolParam(description = "Tình trạng pháp lý: sổ đỏ | sổ hồng | đang chờ")
            String legalStatus,
            @ToolParam(description = "Tình trạng nội thất: đầy đủ | cơ bản | thô")
            String furniture
    ) {
        BuildingSearchFilter filter = new BuildingSearchFilter();
        filter.setTypeCode(typeCode);
        filter.setDistrict(district);
        filter.setWard(ward);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setMinArea(minArea);
        filter.setMaxArea(maxArea);
        filter.setBedrooms(bedrooms);
        filter.setLegalStatus(legalStatus);
        filter.setFurniture(furniture);
        filter = normalizer.normalize(filter);

        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        if (isNotBlank(name)) {
            criteria.add(Criteria.where("name").regex(name.trim(), "i"));
        }
        if (isNotBlank(filter.getTypeCode())) {
            criteria.add(Criteria.where("typeCode").is(filter.getTypeCode()));
        }
        if (isNotBlank(filter.getDistrict())) {
            criteria.add(Criteria.where("district").regex(filter.getDistrict(), "i"));
        }
        if (isNotBlank(filter.getWard())) {
            criteria.add(Criteria.where("ward").regex(filter.getWard(), "i"));
        }
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            Criteria price = Criteria.where("price");
            if (filter.getMinPrice() != null) {
                price = price.gte(BigDecimal.valueOf(filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                price = price.lte(BigDecimal.valueOf(filter.getMaxPrice()));
            }
            criteria.add(price);
        }
        if (filter.getMinArea() != null || filter.getMaxArea() != null) {
            Criteria area = Criteria.where("area");
            if (filter.getMinArea() != null) {
                area = area.gte(filter.getMinArea());
            }
            if (filter.getMaxArea() != null) {
                area = area.lte(filter.getMaxArea());
            }
            criteria.add(area);
        }
        if (filter.getBedrooms() != null) {
            criteria.add(Criteria.where("bedrooms").is(filter.getBedrooms()));
        }
        if (isNotBlank(filter.getLegalStatus())) {
            if ("GIAY_CHUNG_NHAN".equals(filter.getLegalStatus())) {
                criteria.add(Criteria.where("legalStatus").in("GIAY_CHUNG_NHAN", "SO_DO", "SO_HONG"));
            } else {
                criteria.add(Criteria.where("legalStatus").is(filter.getLegalStatus()));
            }
        }
        if (isNotBlank(filter.getFurniture())) {
            criteria.add(Criteria.where("furniture").is(filter.getFurniture()));
        }

        // Tim theo ten thi tra ca BDS da coc/da ban de khach nam ro trang thai hien tai.
        if (!isNotBlank(name)) {
            criteria.add(Criteria.where("saleStatus").is("FOR_SALE"));
        }
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        query.with(Sort.by(Sort.Direction.ASC, "price")).limit(MAX_BUILDINGS);

        List<BuildingEntity> results = mongoTemplate.find(query, BuildingEntity.class);
        if (results.isEmpty()) {
            return "Không tìm thấy BĐS phù hợp với tiêu chí hiện tại.";
        }

        StringBuilder sb = new StringBuilder("Tìm được " + results.size() + " BĐS:\n\n");
        for (BuildingEntity b : results) {
            sb.append("---\n");
            sb.append("ID: ").append(b.getId()).append("\n");
            sb.append("Tên: ").append(nvl(b.getName())).append("\n");
            sb.append("Trạng thái bán: ").append(formatSaleStatus(b.getSaleStatus())).append("\n");
            sb.append("Loại: ").append(b.getTypeCode()).append("\n");
            sb.append("Giá: ").append(formatPrice(b.getPrice())).append("\n");
            sb.append("Diện tích: ").append(b.getArea()).append(" m²\n");
            sb.append("Địa chỉ: ")
                    .append(nvl(b.getStreet())).append(", ")
                    .append(nvl(b.getWard())).append(", ")
                    .append(nvl(b.getDistrict())).append("\n");
            if (b.getBedrooms() != null) {
                sb.append("Phòng ngủ: ").append(b.getBedrooms()).append("\n");
            }
            if (b.getBathrooms() != null) {
                sb.append("Phòng tắm: ").append(b.getBathrooms()).append("\n");
            }
            if (b.getFurniture() != null) {
                sb.append("Nội thất: ").append(b.getFurniture()).append("\n");
            }
            if (b.getLegalStatus() != null) {
                sb.append("Pháp lý: ").append(formatLegalStatus(b.getLegalStatus().name())).append("\n");
            }
            sb.append("Hồ sơ: ")
                    .append(b.getLegalDocumentIds() == null ? 0 : b.getLegalDocumentIds().size())
                    .append(" tài liệu đã upload\n");
            if (b.getNote() != null && !b.getNote().isBlank()) {
                sb.append("Ghi chú: ").append(b.getNote()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Tool(description = """
            Đếm số lượng bất động sản trong hệ thống.
            Dùng khi khách hỏi tổng số BĐS, số lượng theo danh mục hoặc theo trạng thái bán.
            Ví dụ: typeCode='căn hộ' để đếm căn hộ; saleStatus='mở bán' để đếm BĐS đang mở bán.
            Nếu bỏ trống typeCode thì trả tổng số và cơ cấu theo 3 danh mục.
            """)
    public String countProperties(
            @ToolParam(description = "Loại BĐS cần đếm: căn hộ | nhà | đất (hoặc CAN_HO | NGUYEN_CAN | DAT_NEN)")
            String typeCode,
            @ToolParam(description = "Trạng thái bán nếu khách hỏi: mở bán | đã cọc | đã bán (hoặc FOR_SALE | DEPOSIT | SOLD)")
            String saleStatus
    ) {
        BuildingSearchFilter filter = new BuildingSearchFilter();
        filter.setTypeCode(typeCode);
        filter.setSaleStatus(saleStatus);
        filter = normalizer.normalize(filter);

        if (isNotBlank(filter.getTypeCode())) {
            long count = countBuildings(filter.getTypeCode(), filter.getSaleStatus());
            String statusText = isNotBlank(filter.getSaleStatus())
                    ? " (" + formatSaleStatus(filter.getSaleStatus()) + ")"
                    : "";
            return "Hiện hệ thống có " + count + " " + typeLabel(filter.getTypeCode()) + statusText + ".";
        }

        long total = countBuildings(null, filter.getSaleStatus());
        String statusText = isNotBlank(filter.getSaleStatus())
                ? " " + formatSaleStatus(filter.getSaleStatus()).toLowerCase()
                : "";
        StringBuilder sb = new StringBuilder();
        sb.append("Hiện hệ thống có ").append(total).append(" bất động sản").append(statusText).append(".\n");
        sb.append("- Căn hộ: ").append(countBuildings("CAN_HO", filter.getSaleStatus())).append("\n");
        sb.append("- Nhà nguyên căn: ").append(countBuildings("NGUYEN_CAN", filter.getSaleStatus())).append("\n");
        sb.append("- Đất nền: ").append(countBuildings("DAT_NEN", filter.getSaleStatus()));
        return sb.toString();
    }

    @Tool(description = """
            Lưu thông tin liên hệ của khách (SĐT, email) để nhân viên gọi lại.
            Gọi khi khách chủ động cung cấp số điện thoại hoặc đồng ý để lại liên hệ.
            """)
    public String captureLead(String phone, String email, String note) {
        Long sid = SupportChatContextHolder.getSessionId();
        if (sid == null) {
            return "Không xác định được phiên chat — không lưu được.";
        }
        SupportChatSessionEntity session = sessionRepository.findById(sid).orElse(null);
        if (session == null) {
            return "Không tìm thấy phiên chat.";
        }
        if (phone != null && !phone.isBlank()) {
            session.setGuestPhone(phone.trim());
        }
        if (email != null && !email.isBlank()) {
            session.setGuestEmail(email.trim());
        }
        if (note != null && !note.isBlank()) {
            session.setLeadNote(note.trim());
        }
        sessionRepository.save(session);
        return "Đã lưu thông tin liên hệ. Nhân viên sẽ gọi lại khi có thể.";
    }

    @Tool(description = """
            Chuyển cuộc chat cho nhân viên thật khi khách cần tư vấn sâu hoặc yêu cầu gặp người.
            Sau khi gọi, nhân viên có thể thấy thông báo trên dashboard.""")
    public String handoverToStaff(String reason) {
        Long sid = SupportChatContextHolder.getSessionId();
        if (sid == null) {
            return "Không xác định được phiên chat.";
        }
        SupportChatSessionEntity session = sessionRepository.findById(sid).orElse(null);
        if (session == null) {
            return "Không tìm thấy phiên chat.";
        }
        if (session.getUserId() == null) {
            return "Hãy đăng nhập để lưu lịch sử tư vấn và kết nối với chuyên viên.";
        }
        session.setHandoverRequested(true);
        sessionRepository.save(session);
        handoverPublisher.publishHandover(session, reason == null ? "" : reason.trim());
        return "Đã chuyển yêu cầu cho đội nhân viên. Bạn vui lòng chờ trong giây lát — nhân viên sẽ phản hồi trong hội thoại này.";
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private long countBuildings(String typeCode, String saleStatus) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        if (isNotBlank(typeCode)) {
            criteria.add(Criteria.where("typeCode").is(typeCode));
        }
        if (isNotBlank(saleStatus)) {
            criteria.add(Criteria.where("saleStatus").is(saleStatus));
        }
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        return mongoTemplate.count(query, BuildingEntity.class);
    }

    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Liên hệ";
        }
        BigDecimal billion = BigDecimal.valueOf(1_000_000_000L);
        BigDecimal million = BigDecimal.valueOf(1_000_000L);
        if (price.compareTo(billion) >= 0) {
            return price.divide(billion, 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + " tỷ";
        }
        if (price.compareTo(million) >= 0) {
            return price.divide(million, 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + " triệu";
        }
        return price.stripTrailingZeros().toPlainString() + " đồng";
    }

    private static String formatLegalStatus(String status) {
        if ("GIAY_CHUNG_NHAN".equals(status) || "SO_DO".equals(status) || "SO_HONG".equals(status)) {
            return "Giấy chứng nhận";
        }
        if ("PENDING".equals(status)) {
            return "Đang chờ";
        }
        return status;
    }

    private static String formatSaleStatus(String status) {
        if ("DEPOSIT".equals(status)) {
            return "Đã đặt cọc";
        }
        if ("SOLD".equals(status)) {
            return "Đã bán";
        }
        return "Mở bán";
    }

    private static String typeLabel(String typeCode) {
        if ("CAN_HO".equals(typeCode)) {
            return "căn hộ";
        }
        if ("NGUYEN_CAN".equals(typeCode)) {
            return "nhà nguyên căn";
        }
        if ("DAT_NEN".equals(typeCode)) {
            return "đất nền";
        }
        return "bất động sản";
    }
}
