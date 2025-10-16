package com.resturant.repository;


import com.resturant.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient,Long> {

    List<Ingredient> findByNameContainingIgnoreCase(String name);
    Set<Ingredient> findByNameIn(List<String> names);
    Set<Ingredient> findByIdIn(List<Long> ids);
    Optional<Ingredient> findByName(String name);

    boolean existsByName(String name);
    default Set<Ingredient> findByIdIn(Set<Long> ids) {
        return new HashSet<>(findByIdIn(new ArrayList<>(ids)));
    }

}
