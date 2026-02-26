package com.resturant.service;

import com.resturant.dto.SoftDrinkDTO;

import java.util.List;

public interface SoftDrinkService {

    List<SoftDrinkDTO> getAllSoftDrinks();
    SoftDrinkDTO getSoftDrinkById(Long id);
    SoftDrinkDTO createSoftDrink(SoftDrinkDTO softDrinkDTO);
    SoftDrinkDTO updateSoftDrink(Long id, SoftDrinkDTO softDrinkDTO);
    void deleteSoftDrink(Long id);
    void activateSoftDrink(Long id);
    void deactivateSoftDrink(Long id);
}
