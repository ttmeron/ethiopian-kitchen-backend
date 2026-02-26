package com.resturant.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "soft_drinks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoftDrink {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(nullable = false)
        private String name;

        private String description;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal price;
        @Column(name = "size")
        private String size;



        private String iceOption;


        private String imagePath;


        @Column(name = "is_active")
        private Boolean isActive = true;


        private Integer displayOrder = 0;






}
