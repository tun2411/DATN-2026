package com.example.bedatn.controller.admin;

import com.example.bedatn.dto.request.AssignmentBuildingRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.service.AssignmentBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/assign")
@RestController
public class AssignmentBuildingAPI {

    @Autowired
    private AssignmentBuildingService assignmentBuildingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateAssignment(@RequestBody AssignmentBuildingRequest request) {
        ApiResponse<Void> body = new ApiResponse<>();
        if (request.getBuildingId() == null) {
            body.setMessage("Validate Failed");
            return ResponseEntity.badRequest().body(body);
        }
        try {
            assignmentBuildingService.updateAssignment(request);
        } catch (IllegalArgumentException ex) {
            body.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
        body.setMessage("Assignment Building Updated Successfully");
        return ResponseEntity.ok().body(body);
    }
}
