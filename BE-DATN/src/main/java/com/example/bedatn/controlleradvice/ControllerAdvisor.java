package com.example.bedatn.controlleradvice;

import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.exception.ValidateDataBuildingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(ValidateDataBuildingException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidateDataBuildingException(ValidateDataBuildingException ex) {
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Validation Failed");
        body.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.toList());
        ApiResponse<List<String>> body = new ApiResponse<>();
        body.setMessage("Validation Failed");
        body.setDetail(String.join("; ", errors));
        body.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Business Error");
        body.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Validation Failed");
        body.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage(ex.getStatusCode().is4xxClientError() ? "Request Failed" : "Internal Server Error");
        body.setDetail(ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        ex.printStackTrace();
        ApiResponse<Void> body = new ApiResponse<>();
        body.setMessage("Internal Server Error");
        body.setDetail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
