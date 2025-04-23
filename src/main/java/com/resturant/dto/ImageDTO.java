package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Image information and metadata")
public class ImageDTO {
    @Schema(
            description = "Original filename of the uploaded image",
            example = "doro_wot.jpg"
    )
    private String filename;
    @Schema(
            description = "Full URL to access the image",
            example = "https://ethiopiankitchen.com/uploads/doro_wot.jpg",
            format = "uri"
    )
    private String url;
    @Schema(
            description = "Size of the image file in bytes",
            example = "102400",
            minimum = "0"
    )
    private long size;
    @Schema(
            description = "MIME type of the image",
            example = "image/jpeg",
            allowableValues = {
                    "image/jpeg",
                    "image/png",
                    "image/gif",
                    "image/webp"
            }
    )
    private String contentType;
    @Schema(
            description = "Timestamp of when the image was last modified",
            example = "2023-07-20T14:30:00",
            format = "date-time"
    )
    private LocalDateTime lastModified;

}
