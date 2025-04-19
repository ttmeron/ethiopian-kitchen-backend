package com.resturant.service;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.DeliveryStatus;

import java.util.List;

public interface DeliveryService {


    DeliveryDTO createDelivery(DeliveryDTO deliveryDTO);
    DeliveryDTO getDeliveryById(Long id);
    List<DeliveryDTO> getAllDelivery();
    DeliveryDTO updateDelivery(Long id, DeliveryDTO deliveryDTO);
    void deleteDelivery(Long id);
    DeliveryDTO getDeliveryByOrderId(Long orderId);
    List<DeliveryDTO> getDeliveriesByStatus(DeliveryStatus status);
    DeliveryDTO updateDeliveryStatus(Long id, DeliveryStatus status);
}
