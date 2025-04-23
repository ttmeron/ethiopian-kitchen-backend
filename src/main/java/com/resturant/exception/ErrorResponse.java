package com.resturant.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ErrorResponse {

    private String message;
    private List<FieldErrorDetail> errors;
    private String path;
    private long timestamp;

    public ErrorResponse(String message, BindingResult bindingResult, String path) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
        this.errors = bindingResult.getFieldErrors().stream()
                .map(FieldErrorDetail::new)
                .collect(Collectors.toList());
    }


    public static class FieldErrorDetail {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public FieldErrorDetail(FieldError fieldError) {
            this.field = fieldError.getField();
            this.message = fieldError.getDefaultMessage();
            this.rejectedValue = fieldError.getRejectedValue();
        }

        // Getters
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}
