package com.resturant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
//@AllArgsConstructor
@Schema(description = "Standardized error response format")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private final int status ;

    @Schema(description = "Error code", example = "FILE_NOT_FOUND")
    private String code;

    @Schema(description = "Human-readable error message", example = "File not found")
    private String message;

    @Schema(description = "Filename affected by the operation", example = "doro_wot.jpg")
    private String filename;

    @Schema(description = "Timestamp of the error", example = "2023-07-20T12:00:00Z")
    private Instant timestamp;



    // Constructor
    public ErrorResponse(HttpStatus status, String code, String message, String filename) {
        this.status = status.value();
        this.code = code;
        this.message = message;
        this.filename = filename;
        this.timestamp = Instant.now();
    }

}
