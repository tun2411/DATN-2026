package com.example.bedatn.service;

import com.example.bedatn.dto.request.LegalDocumentRequest;
import com.example.bedatn.dto.response.LegalDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LegalDocumentService {
    LegalDocumentResponse upload(String buildingId, MultipartFile file, LegalDocumentRequest metadata, String uploadedBy);

    List<LegalDocumentResponse> listByBuilding(String buildingId);

    LegalDocumentResponse getOne(String id);

    LegalDocumentResponse verify(String id, String verifiedBy);

    LegalDocumentResponse reject(String id, String reason);

    void delete(String id);

    List<LegalDocumentResponse> expiringSoon(int days);
}
