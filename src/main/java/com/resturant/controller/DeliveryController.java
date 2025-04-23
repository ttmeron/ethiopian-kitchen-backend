package com.resturant.controller;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.DeliveryStatus;
import com.resturant.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/deliveries")

@Tag(name = "Delivery", description = "Operations related to order deliveries")
public class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    @PostMapping
    @Operation(
            summary = "Schedule a new delivery",
            description = "Create a delivery record for an order"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Delivery successfully scheduled",
                    content = @Content(schema = @Schema(implementation = DeliveryDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid delivery information"
            )
    })
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO){

        DeliveryDTO createDelivery = deliveryService.createDelivery(deliveryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createDelivery);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get delivery by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Found the delivery record",
            content = @Content(schema = @Schema(implementation = DeliveryDTO.class))
    )
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id){

        DeliveryDTO deliveryDTO = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(deliveryDTO);
    }

    @GetMapping
    @Operation(
            summary = "Get all deliveries",
            description = "Retrieve all deliveries, optionally filtered by status"
    )
    @Parameter(
            name = "status",
            description = "Filter deliveries by status",

            in = ParameterIn.QUERY,
            schema = @Schema(implementation = DeliveryStatus.class),
            example = "IN_TRANSIT"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved deliveries",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeliveryDTO.class))))
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries(@RequestPart(required = false) DeliveryStatus status){

        List<DeliveryDTO> deliveries = status != null
                ? deliveryService.getDeliveriesByStatus(status)
                : deliveryService.getAllDelivery();

        return ResponseEntity.ok(deliveries);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update a delivery", description = "Update delivery information by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery updated successfully",
                    content = @Content(schema = @Schema(implementation = DeliveryDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid delivery data provided"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Delivery not found")
    })
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable Long id,
                                                      @Valid @RequestBody DeliveryDTO deliveryDTO){

        DeliveryDTO updateDelivery = deliveryService.updateDelivery(id,deliveryDTO);
        return ResponseEntity.ok(updateDelivery);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery", description = "Remove a delivery record by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Delivery deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery by order ID",
            description = "Retrieve delivery information for a specific order")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved delivery",
                    content = @Content(schema = @Schema(implementation = DeliveryDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Delivery not found for the given order ID")
    })
    public ResponseEntity<DeliveryDTO> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryDTO deliveryDTO = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(deliveryDTO);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryDTO> updateDeliveryStatus(
            @Parameter(description = "ID of the delivery to update", example = "101")
            @PathVariable Long id,
            @Parameter(
                    description = "New status value",
                    example = "IN_TRANSIT",
                    schema = @Schema(allowableValues = {"IN_TRANSIT", "DELIVERED", "CANCELLED"})
            )
            @RequestParam DeliveryStatus status) {

        DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, status);
        return ResponseEntity.ok(updatedDelivery);
    }
}
