package com.resturant.service;


import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.Ingredient;
import com.resturant.entity.OrderItem;
import com.resturant.entity.OrderItemIngredient;
import com.resturant.mapper.OrderItemIngredientMapper;
import com.resturant.repository.IngredientRepository;
import com.resturant.repository.OrderItemIngredientRepository;
import com.resturant.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemIngredientServiceImpl implements OrderItemIngredientService{

    @Autowired
    OrderItemIngredientRepository repository;
    @Autowired
    OrderItemIngredientMapper mapper;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    OrderItemRepository orderItemRepository;

    @Override
    public OrderItemIngredientDTO createOrderItemIngredient(OrderItemIngredientDTO orderItemIngredientDTO) {

        OrderItemIngredient entity = mapper.toEntity(orderItemIngredientDTO);

        Ingredient ingredient = ingredientRepository.findById(orderItemIngredientDTO.getIngredientId())
                .orElseThrow(()-> new RuntimeException("Ingredient not found with ID: " + orderItemIngredientDTO.getIngredientId()));

        entity.setIngredient(ingredient);

        OrderItem item = orderItemRepository.findById(orderItemIngredientDTO.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("OrderItem not found with ID: " + orderItemIngredientDTO.getOrderItemId()));
        entity.setOrderItem(item);
        OrderItemIngredient saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public List<OrderItemIngredientDTO> findByOrderItemId(Long orderItemId) {
        List<OrderItemIngredient> ingredients = repository.findByOrderItemId(orderItemId);
        return mapper.toDTOList(ingredients);
    }

    @Override
    public List<OrderItemIngredientDTO> getAllOrderItemIngredient() {

        return mapper.toDTOList(repository.findAll());
    }


    @Override
    public void deleteOrderItemIngredient(Long id) {
        repository.deleteById(id);

    }

    public OrderItemIngredient toEntity(OrderItemIngredientDTO dto) {
        OrderItemIngredient orderItemIngredient = new OrderItemIngredient();

        // Set Ingredient (fetch by ID)
        Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        orderItemIngredient.setIngredient(ingredient);

        // Set Extra Cost and Quantity
        orderItemIngredient.setExtraCost(dto.getExtraCost());
        orderItemIngredient.setQuantity(dto.getQuantity());

        return orderItemIngredient;
    }
}
