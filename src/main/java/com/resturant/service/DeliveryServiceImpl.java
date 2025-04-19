package com.resturant.service;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.Delivery;
import com.resturant.entity.DeliveryStatus;
import com.resturant.entity.Order;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.DeliveryMapper;
import com.resturant.repository.DeliveryRepository;
import com.resturant.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryServiceImpl implements DeliveryService{

    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    DeliveryMapper deliveryMapper;

    @Override
    public DeliveryDTO createDelivery(DeliveryDTO deliveryDTO) {
        // Validate order exists
        Order order = orderRepository.findById(deliveryDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + deliveryDTO.getOrderId()));

        // Check if delivery already exists for this order
        if (deliveryRepository.existsByOrderId(deliveryDTO.getOrderId())) {
            throw new IllegalStateException("Delivery already exists for order id: " + deliveryDTO.getOrderId());
        }

        Delivery delivery = deliveryMapper.toEntity(deliveryDTO);
        delivery.setOrder(order);
        delivery.setStatus(DeliveryStatus.SCHEDULED); // Default status

        Delivery savedDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toDTO(savedDelivery);
    }

    @Override
    public DeliveryDTO getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
        return deliveryMapper.toDTO(delivery);
    }

    @Override
    public List<DeliveryDTO> getAllDelivery() {
        return deliveryRepository.findAll()
                .stream()
                .map(deliveryMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    public DeliveryDTO updateDelivery(Long id, DeliveryDTO deliveryDTO) {
        Delivery existingDelivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        // Update only modifiable fields
        existingDelivery.setDeliveryAddress(deliveryDTO.getDeliveryAddress());
        existingDelivery.setDeliveryTime(deliveryDTO.getDeliveryTime());
        existingDelivery.setStatus(DeliveryStatus.valueOf(deliveryDTO.getStatus()));

        // If changing order
        if (!existingDelivery.getOrder().getId().equals(deliveryDTO.getOrderId())) {
            Order newOrder = orderRepository.findById(deliveryDTO.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + deliveryDTO.getOrderId()));
            existingDelivery.setOrder(newOrder);
        }

        Delivery updatedDelivery = deliveryRepository.save(existingDelivery);
        return deliveryMapper.toDTO(updatedDelivery);
    }

    @Override
    public void deleteDelivery(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        // Remove reference from order
        delivery.getOrder().setDelivery(null);

        deliveryRepository.delete(delivery);

    }
    @Override
    public DeliveryDTO updateDeliveryStatus(Long id, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found with id: " + id));

        delivery.setStatus(status);
        return deliveryMapper.toDTO(delivery);

    }
    @Override
    public List<DeliveryDTO> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status)
                .stream()
                .map(deliveryMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public DeliveryDTO getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .map(deliveryMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No delivery found for order ID: " + orderId));
    }
}
