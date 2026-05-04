package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.request.CustomerRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.service.CustomerService;
import com.example.bedatn.utils.VietnamPhoneUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping()
@CrossOrigin(origins = "*")
public class ContactAPI {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/lien-he")
    public ResponseEntity<ApiResponse<Map<String, String>>> contactUser(@RequestBody CustomerRequest request) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        try {
            Map<String, String> errors = new HashMap<>();
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                errors.put("fullName", "Họ và tên không được để trống");
            }
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                errors.put("phone", "Số điện thoại không được để trống");
            } else if (!VietnamPhoneUtils.isValidMobile10Digits(request.getPhone())) {
                errors.put("phone", "Số điện thoại không hợp lệ");
            } else if (customerService.existsByPhone(request.getPhone(), null)) {
                errors.put("phone", "Số điện thoại đã tồn tại trong hệ thống");
            }
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|vn|net|org)$")) {
                    errors.put("email", "Email không đúng định dạng");
                } else if (customerService.existsByEmail(request.getEmail(), null)) {
                    errors.put("email", "Email đã tồn tại");
                }
            }
            if (!errors.isEmpty()) {
                response.setMessage("Validation failed");
                response.setDetail("Lỗi xác thực dữ liệu");
                response.setData(errors);
                return ResponseEntity.badRequest().body(response);
            }
            customerService.contactUser(request);
            response.setMessage("Gửi thông tin thành công");
            response.setData(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Lỗi server");
            response.setDetail("Lỗi: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
