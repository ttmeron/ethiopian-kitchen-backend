package com.resturant.repository;

import com.resturant.entity.SoftDrink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SoftDrinkRepository extends JpaRepository<SoftDrink , Long> {
    List<SoftDrink> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT s FROM SoftDrink s WHERE s.isActive = true ORDER BY s.displayOrder ASC")
    List<SoftDrink> findAllActive();

    @Query("SELECT s FROM SoftDrink s WHERE s.id = :id AND s.isActive = true")
    Optional<SoftDrink> findActiveById(@Param("id") Long id);

}
