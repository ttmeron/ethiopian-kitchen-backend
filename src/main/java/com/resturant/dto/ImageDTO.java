package com.resturant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {

    private String filename;
    private String url;
    private long size;
    private String contentType;
    private LocalDateTime lastModified;

}
