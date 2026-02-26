package com.resturant.repository;


import com.resturant.entity.Order;
import com.resturant.entity.OrderStatus;
import com.resturant.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {


    List<Order> findByUserId(Long userId);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems LEFT JOIN FETCH o.delivery WHERE o.id = :id")
    Optional<Order> findByIdWithRelations(@Param("id") Long id);

    @Query(" SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.food LEFT JOIN FETCH oi.drink LEFT JOIN FETCH o.delivery WHERE o.id = :id")
    Optional<Order> findByIdWithAllItems(@Param("id") Long id);


    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    List<Order> findByPaymentStatusAndStatus(PaymentStatus paymentStatus, OrderStatus orderStatus);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserEmail(String email);
    Optional<Order> findByTrackingToken(String trackingToken);

    List<Order> findByUser_EmailAndStatus(String email, OrderStatus status);
    Optional<Order> findByGuestTokenAndStatus(String guestToken, OrderStatus status);
    Optional<Order> findFirstByGuestTokenAndStatusOrderByCreatedAtDesc(String guestToken, OrderStatus status);



    @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE u.email = :email OR o.guestEmail = :email")
    List<Order> findByUserEmailOrGuestEmail(@Param("email") String email1, @Param("email") String email2);







}
