package com.resturant.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse response = new ErrorResponse(
                "Validation failed",
                ex.getBindingResult(),
                path
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
