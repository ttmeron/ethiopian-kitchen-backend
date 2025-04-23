package com.resturant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
@Getter
@Schema(description = "Standardized success response format")
public class SuccessResponse {

    @Schema(description = "Response status", example = "success")
    private final String status = "success";

    @Schema(description = "Status code", example = "FILE_DELETED")
    private String code;

    @Schema(description = "Human-readable message", example = "File deleted successfully")
    private String message;

    @Schema(description = "Filename affected by the operation", example = "doro_wot.jpg")
    private String filename;

    @Schema(description = "Timestamp of the response", example = "2023-07-20T12:00:00Z")
    private Instant timestamp;

    // Constructor
    public SuccessResponse(String code, String message, String filename) {
        this.code = code;
        this.message = message;
        this.filename = filename;
        this.timestamp = Instant.now();
    }
}
