package com.resturant.entity;


import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
;


@Entity
@Data
@Table(name = "deliveries")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    @Column(nullable = false)
    private LocalDateTime deliveryTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;



}
