package com.resturant.controller;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.DeliveryStatus;
import com.resturant.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO){

        DeliveryDTO createDelivery = deliveryService.createDelivery(deliveryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createDelivery);
    }

    @GetMapping("{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id){

        DeliveryDTO deliveryDTO = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(deliveryDTO);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries(@RequestPart(required = false) DeliveryStatus status){

        List<DeliveryDTO> deliveries = status != null
                ? deliveryService.getDeliveriesByStatus(status)
                : deliveryService.getAllDelivery();

        return ResponseEntity.ok(deliveries);
    }

    @PutMapping("{id}")
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable Long id,
                                                      @Valid @RequestBody DeliveryDTO deliveryDTO){

        DeliveryDTO updateDelivery = deliveryService.updateDelivery(id,deliveryDTO);
        return ResponseEntity.ok(updateDelivery);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryDTO> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryDTO deliveryDTO = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(deliveryDTO);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryDTO> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {

        DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, status);
        return ResponseEntity.ok(updatedDelivery);
    }
}
