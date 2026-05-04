package com.example.bedatn.controller.admin;

import com.example.bedatn.constant.SystemConstant;
import com.example.bedatn.dto.request.PasswordChangeRequest;
import com.example.bedatn.dto.request.UserRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.UserListPageResponse;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.exception.MyException;
import com.example.bedatn.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserAPI {

    @Autowired
    private IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserListPageResponse>> listUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        List<UserResponse> items = userService.getUsers(search, pageable);
        int total = userService.getTotalItems(search);
        int totalPages = (total == 0) ? 0 : (int) Math.ceil((double) total / pageable.getPageSize());

        UserListPageResponse data = new UserListPageResponse();
        data.setItems(items);
        data.setTotalItems(total);
        data.setPage(pageable.getPageNumber());
        data.setPageSize(pageable.getPageSize());
        data.setTotalPages(totalPages);

        ApiResponse<UserListPageResponse> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") long id) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setMessage("Completed");
        response.setData(userService.findUserById(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUsers(@RequestBody UserRequest newUser) {
        return ResponseEntity.ok(userService.insert(newUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUsers(@PathVariable("id") long id, @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<String> changePasswordUser(@PathVariable("id") long id, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            userService.updatePassword(id, passwordChangeRequest);
            return ResponseEntity.ok(SystemConstant.UPDATE_SUCCESS);
        } catch (MyException e) {
            return ResponseEntity.ok(e.getMessage());
        }
    }

    @PutMapping("/password/{id}/reset")
    public ResponseEntity<UserResponse> resetPassword(@PathVariable("id") long id) {
        return ResponseEntity.ok(userService.resetPassword(id));
    }

    @PutMapping("/profile/{username}")
    public ResponseEntity<UserResponse> updateProfileOfUser(@PathVariable("username") String username, @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateProfileOfUser(username, userRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@RequestBody long[] idList) {
        if (idList.length > 0) {
            userService.delete(idList);
        }
        return ResponseEntity.noContent().build();
    }
}
