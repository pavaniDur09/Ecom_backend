package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.dto.OrderDTO;
import com.ecommerce.model.dto.OrderSummaryDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.User;
import com.ecommerce.model.enums.OrderStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // In-memory counter for the demo order-number sequence within a run.
    // A production system would use a DB sequence or UUID instead.
    private final AtomicInteger orderSequence = new AtomicInteger(1);

    /**
     * Creates an order end-to-end as a single atomic transaction:
     *   1. validate the user and every requested product
     *   2. decrement stock for each line item (fails fast on insufficient stock)
     *   3. persist the order + items together (cascade = ALL on Order.orderItems)
     *
     * Propagation.REQUIRED (the default) ensures that if ANY step throws -
     * e.g. InsufficientStockException on the second item - the whole order,
     * including stock already decremented for the first item, is rolled back.
     * Isolation.READ_COMMITTED avoids reading stock levels concurrent
     * transactions haven't committed yet, while still allowing good throughput
     * (the row-level lock taken by the UPDATE on `stock` serializes concurrent
     * purchases of the same product).
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OrderDTO createOrder(OrderDTO.CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (OrderDTO.CreateOrderItemRequest itemRequest : request.getItems()) {
            // findById locks nothing by default; for high-contention catalogs consider
            // @Lock(LockModeType.PESSIMISTIC_WRITE) here to serialize stock updates.
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            product.decreaseStock(itemRequest.getQuantity()); // throws InsufficientStockException -> rolls back whole order

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItem.recalculateSubtotal();

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(order.calculateTotal());
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return orderRepository.findByUser(user, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getOrderSummariesForUser(Long userId) {
        return orderRepository.findOrderSummariesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public OrderDTO getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumberWithUser(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return toDTO(order);
    }

    /**
     * Cancels an order and restocks every line item, all inside one
     * transaction so partial cancellations can never happen.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel a delivered order");
        }

        for (OrderItem item : order.getOrderItems()) {
            item.getProduct().increaseStock(item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);

        return toDTO(order);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDailySalesReport(LocalDateTime since) {
        return orderRepository.getDailyOrderReport(since);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + datePart + "-" + String.format("%03d", orderSequence.getAndIncrement());
    }

    private OrderDTO toDTO(Order order) {
        List<OrderDTO.OrderItemDTO> items = order.getOrderItems().stream()
                .map(oi -> OrderDTO.OrderItemDTO.builder()
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .subtotal(oi.getSubtotal())
                        .build())
                .toList();

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
