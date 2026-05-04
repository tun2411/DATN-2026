package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.request.UserRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.request.LoginRequest;
import com.example.bedatn.dto.response.LoginResponse;
import com.example.bedatn.dto.response.MyProfileResponse;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.documents.CustomerEntity;
import com.example.bedatn.enums.Status;
import com.example.bedatn.repository.CustomerRepository;
import com.example.bedatn.service.impl.UserService;
import com.example.bedatn.util.PhoneUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterAPI {
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        ApiResponse<LoginResponse> response = new ApiResponse<>();
        if (loginRequest == null || loginRequest.getLogin() == null || loginRequest.getLogin().trim().isEmpty()) {
            response.setMessage("Validation failed");
            response.setDetail("Tên đăng nhập/email không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            response.setMessage("Validation failed");
            response.setDetail("Mật khẩu không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        UserResponse user = userService.authenticate(loginRequest.getLogin(), loginRequest.getPassword());
        if (user == null) {
            response.setMessage("Login failed");
            response.setDetail("Sai thông tin đăng nhập hoặc tài khoản đã bị khóa");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        LoginResponse data = new LoginResponse();
        data.setId(user.getId());
        data.setUserName(user.getUserName());
        data.setFullName(user.getFullName());
        data.setEmail(user.getEmail());
        data.setRoleCode(user.getRoleCode());
        String email = user.getEmail() == null ? null : user.getEmail().trim();
        CustomerEntity customer =
                email == null || email.isEmpty() ? null : customerRepository.findFirstByEmailAndActive(email, 1L);
        data.setCustomerId(customer != null ? customer.getId() : null);
        response.setMessage("Đăng nhập thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ApiResponse<MyProfileResponse>> myProfile(@RequestParam("userId") Long userId) {
        ApiResponse<MyProfileResponse> response = new ApiResponse<>();
        if (userId == null) {
            response.setMessage("Validation failed");
            response.setDetail("userId không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        UserResponse user = userService.findUserById(userId);
        if (user == null) {
            response.setMessage("Not found");
            response.setDetail("Không tìm thấy người dùng");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        String email = user.getEmail() == null ? null : user.getEmail().trim();
        CustomerEntity customer =
                email == null || email.isEmpty() ? null : customerRepository.findFirstByEmailAndActive(email, 1L);
        MyProfileResponse data = new MyProfileResponse();
        data.setId(user.getId());
        data.setUserName(user.getUserName());
        data.setFullName(user.getFullName());
        data.setEmail(user.getEmail());
        data.setRoleCode(user.getRoleCode());
        data.setCustomerId(customer != null ? customer.getId() : null);
        response.setMessage("Completed");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerUser(@RequestBody UserRequest userRequest) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();

        try {
            Map<String, String> errors = new HashMap<>();
            if (userRequest.getUserName() == null || userRequest.getUserName().trim().isEmpty()) {
                errors.put("userName", "Tên đăng nhập không được để trống");
            }
            if (userRequest.getFullName() == null || userRequest.getFullName().trim().isEmpty()) {
                errors.put("fullName", "Họ và tên không được để trống");
            }
            if (userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {
                errors.put("password", "Mật khẩu không được để trống");
            }
            if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
                errors.put("email", "Email không được để trống");
            } else {
                userRequest.setEmail(userRequest.getEmail().trim());
                if (!userRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|vn|net|org)$")) {
                    errors.put("email", "Email không hợp lệ");
                }
            }
            String phoneNorm = PhoneUtils.normalizeVietnamPhone(
                    userRequest.getPhone() == null ? "" : userRequest.getPhone());
            if (phoneNorm.isEmpty()) {
                errors.put("phone", "Số điện thoại không được để trống");
            } else if (!PhoneUtils.isValidVietnamMobile(phoneNorm)) {
                errors.put("phone", "Số điện thoại không hợp lệ (nhập 10–11 số, ví dụ 0912345678)");
            } else if (userService.existsActiveUserWithPhone(phoneNorm)) {
                errors.put("phone", "Số điện thoại đã được đăng ký cho tài khoản khác");
            }
            if (userService.existsByUserName(userRequest.getUserName())) {
                errors.put("userName", "Tên đăng nhập đã tồn tại");
            }
            if (userService.existsByEmail(userRequest.getEmail())) {
                errors.put("email", "Email đã tồn tại");
            }

            if (!errors.isEmpty()) {
                response.setMessage("Validation failed");
                response.setDetail("Lỗi xác thực dữ liệu");
                response.setData(errors);
                return ResponseEntity.badRequest().body(response);
            }

            // Đăng ký public luôn là USER, không nhận role từ client.
            userRequest.setRoleCode("USER");
            userRequest.setPhone(phoneNorm);
            userService.registerUser(userRequest);
            createCustomerIfMissingForNewUser(
                    userRequest.getFullName().trim(),
                    phoneNorm,
                    userRequest.getEmail());

            response.setMessage("Đăng ký thành công");
            response.setDetail("Người dùng đã được tạo");
            response.setData(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Lỗi server");
            response.setDetail("Lỗi: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sau đăng ký: tạo bản ghi {@link CustomerEntity} tối thiểu (tên, SĐT, email) để đăng nhập gắn được
     * {@code customerId} (theo email) va xem tien do giao dich. Bo qua neu da co customer active trung email.
     * Khong chan theo SDT: du lieu mau hoac lead co the trung SDT khien user moi khong duoc tao customer
     */
    private void createCustomerIfMissingForNewUser(String fullName, String phoneNorm, String email) {
        if (email == null || email.isEmpty() || phoneNorm == null || phoneNorm.isEmpty()) {
            return;
        }
        CustomerEntity byEmail = customerRepository.findFirstByEmailAndActive(email, 1L);
        if (byEmail != null) {
            byEmail.setStatus(Status.getNameByStatus(Status.DA_XU_LY));
            customerRepository.save(byEmail);
            return;
        }
        CustomerEntity customer = new CustomerEntity();
        customer.setId(System.currentTimeMillis());
        customer.setFullName(fullName);
        customer.setPhone(phoneNorm);
        customer.setEmail(email);
        customer.setActive(1L);
        customer.setStatus(Status.getNameByStatus(Status.DA_XU_LY));
        customerRepository.save(customer);
    }
}
