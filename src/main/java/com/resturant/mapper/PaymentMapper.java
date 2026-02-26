package com.resturant.mapper;

import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.resturant.entity.Payment;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaymentMapper {


    @Mapping(target = "paymentId", source = "paymentIntentId")

    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentDate", expression = "java(java.time.LocalDateTime.now())")
    Payment toEntity(PaymentRequestDTO request, String paymentIntentId, BigDecimal amount);


    @Mapping(target = "paymentId", source = "payment.paymentId")
    @Mapping(target = "status", source = "payment.status")
    @Mapping(target = "clientSecret", ignore = true)
    PaymentResponseDTO toDto(Payment payment, @Context String clientSecret);

    @AfterMapping
    default void setClientSecret(@MappingTarget PaymentResponseDTO dto, @Context String clientSecret) {
        dto.setClientSecret(clientSecret);
        System.out.println("Setting clientSecret via @AfterMapping: " + clientSecret);
    }
}