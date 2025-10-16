package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.response.GuestOrderResponseDTO;
import com.resturant.entity.User;

public interface GuestOrderService {
    OrderDTO createGuestOrder(GuestOrderDTO guestOrderDTO);

     OrderDTO mapGuestOrderToOrderDTO(GuestOrderDTO guestOrderDTO);
    GuestOrderResponseDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, User guest, String paymentIntentId);

}
