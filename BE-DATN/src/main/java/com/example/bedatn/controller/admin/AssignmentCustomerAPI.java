package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.request.AssignmentCustomerRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.service.AssignmentCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/assignCustomer")
@RestController
public class AssignmentCustomerAPI {
    @Autowired
    private AssignmentCustomerService assignmentCustomerService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateAssignment(@RequestBody AssignmentCustomerRequest request) {
        ApiResponse<Void> body = new ApiResponse<>();
        if (request.getCustomerId() == null) {
            body.setMessage("Validate Failed");
            return ResponseEntity.badRequest().body(body);
        }
        assignmentCustomerService.updateAssignment(request);
        body.setMessage("Assignment Customer Updated Successfully");
        return ResponseEntity.ok().body(body);
    }
}
