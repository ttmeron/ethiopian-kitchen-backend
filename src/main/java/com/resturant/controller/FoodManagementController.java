package com.resturant.controller;


import com.resturant.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods/admin")
public class FoodManagementController {


    @Autowired
    private FoodService foodService;



}
