package com.resturant.controller;


import com.resturant.dto.OrderDTO;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")

@Tag(name = "Order", description = "Ethiopian Kitchen Order API")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    @Operation(summary = "Add a new order item")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderDTO orderDTO,  BindingResult bindingResult, HttpServletRequest request){

        // Manual validation for foodId
        for (int i = 0; i < orderDTO.getOrderItems().size(); i++) {
            if (orderDTO.getOrderItems().get(i).getFoodId() == null) {
                bindingResult.rejectValue("orderItems["+i+"].foodId",
                        "NotNull",
                        "Food ID is required");
            }
        }

        if (bindingResult.hasErrors()) {
            // Return customized error response
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation failed", bindingResult,request.getRequestURI()));
        }

        return ResponseEntity.ok(orderService.placeOrder(orderDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a order  by ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    @Operation(summary = "Get all order items",
            description = "Returns a list of all Ethiopian Kitchen orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order")
    public ResponseEntity<List<OrderDTO>> getAllOrder(){
        return ResponseEntity.ok(orderService.getAllOrder());
    }

    @PutMapping("/{id}")
    @Operation(summary = "update order")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.updateOrder(id,orderDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "delete unwanted order")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
