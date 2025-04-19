package com.resturant.repository;


import com.resturant.entity.Delivery;
import com.resturant.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
    long countByOrderId(Long orderId);
    @Query("SELECT d FROM Delivery d WHERE d.order.id = :orderId AND d.status = 'SCHEDULED'")
    Optional<Delivery> findScheduledDeliveryForOrder(@Param("orderId") Long orderId);

    List<Delivery> findByStatus(DeliveryStatus status);
}
