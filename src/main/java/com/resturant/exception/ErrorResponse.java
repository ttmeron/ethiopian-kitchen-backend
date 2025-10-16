package com.resturant.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Data
public class ErrorResponse {

    private String message;
    private List<FieldErrorDetail> errors;
    private String path;
    private long timestamp;
    private int status;


    public ErrorResponse(String message, BindingResult bindingResult, String path, HttpStatus status) {
        this.message = message;
        this.errors = bindingResult.getFieldErrors().stream()
                .map(FieldErrorDetail ::new)
                .collect(Collectors.toList());
        this.path = path;
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status.value();
    }

    public ErrorResponse(String message, String path) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
        this.errors = null;
    }
    public ErrorResponse(String message, String path, HttpStatus status) {
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status.value();
        this.errors = null;
    }



    @Data
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private final String object;
        private final String field;
        private final String message;
        private final Object rejectedValue;
        private final String code;

        public FieldErrorDetail(FieldError fieldError) {
            this.object = fieldError.getObjectName();
            this.field = fieldError.getField();
            this.message = fieldError.getDefaultMessage();
            this.rejectedValue = fieldError.getRejectedValue();
            this.code = fieldError.getCode();
        }
    }
}
