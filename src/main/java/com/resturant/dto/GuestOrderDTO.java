package com.resturant.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Builder
public class GuestOrderDTO {

    @NotBlank
    private String guestEmail;
    private String guestToken;


    @NotBlank
    private  String guestName;

    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemDTO> orderItemDTOS;

    private String specialInstructions;

    private BigDecimal totalAmount;
}
