package com.resturant.repository;


import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.OrderItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemIngredientRepository extends JpaRepository<OrderItemIngredient, Long> {

    List<OrderItemIngredient> findByOrderItemId(Long OrderItemId);
    OrderItemIngredientDTO save(OrderItemIngredientDTO dto);
    void deleteById(Long id);
}
