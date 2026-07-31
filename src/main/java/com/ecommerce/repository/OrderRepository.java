package com.ecommerce.repository;

import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.User;
import com.ecommerce.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user with pagination
    Page<Order> findByUser(User user, Pageable pageable);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders within a date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Find order by order number, eagerly fetching the user in one query (avoids N+1)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithUser(@Param("orderNumber") String orderNumber);

    // Lightweight projection for a user's order history (see OrderSummaryDTO)
    @Query("""
           SELECT new com.ecommerce.model.dto.OrderSummaryDTO(
                   o.id, o.orderNumber, o.totalAmount, o.status, o.createdAt)
           FROM Order o
           WHERE o.user.id = :userId
           ORDER BY o.createdAt DESC
           """)
    List<com.ecommerce.model.dto.OrderSummaryDTO> findOrderSummariesByUserId(@Param("userId") Long userId);

    // Native query for the daily sales report shown in the sample output
    @Query(value = """
        SELECT DATE(o.created_at) as order_date,
               COUNT(*) as total_orders,
               SUM(o.total_amount) as total_revenue
        FROM orders o
        WHERE o.created_at >= :startDate
        GROUP BY DATE(o.created_at)
        ORDER BY order_date DESC
        """, nativeQuery = true)
    List<Object[]> getDailyOrderReport(@Param("startDate") LocalDateTime startDate);
}
