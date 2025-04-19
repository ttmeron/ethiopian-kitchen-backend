package com.resturant.repository;


import com.resturant.entity.Food;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByNameContainingIgnoreCase(String name);
    Optional<Food> findByName(String name);
    @EntityGraph(attributePaths = {"ingredients"})
    Optional<Food> findWithIngredientsById(Long id);
}
