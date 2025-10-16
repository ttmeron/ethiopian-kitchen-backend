package com.resturant.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GuestOrderDTO {

    @NotBlank
    private String guestEmail;

    @NotBlank
    private  String guestName;

    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemDTO> orderItemDTOS;

    private String specialInstructions;

    private double totalAmount;
}
