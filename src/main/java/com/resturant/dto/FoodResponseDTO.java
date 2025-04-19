package com.resturant.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodResponseDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imagePath;
    private List<String> ingredientNames;
    private LocalDateTime createdAt;
//    private List<Long> ingredientIds;


}
