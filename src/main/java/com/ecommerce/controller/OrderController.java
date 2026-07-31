package com.ecommerce.controller;

import com.ecommerce.model.dto.OrderDTO;
import com.ecommerce.model.dto.OrderSummaryDTO;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders - creates the order, decrements stock, all in one transaction
    @PostMapping
    public ResponseEntity<OrderDTO> create(@RequestBody OrderDTO.CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    // GET /api/orders?userId=1 - paginated order history for a user
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getUserOrders(@RequestParam Long userId, Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersForUser(userId, pageable));
    }

    // GET /api/orders/summary?userId=1 - lightweight order history projection
    @GetMapping("/summary")
    public ResponseEntity<List<OrderSummaryDTO>> getUserOrderSummaries(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrderSummariesForUser(userId));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDTO> getByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getByOrderNumber(orderNumber));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // GET /api/orders/report/daily?since=2024-01-01T00:00:00
    @GetMapping("/report/daily")
    public ResponseEntity<List<Map<String, Object>>> dailyReport(
            @RequestParam(required = false) LocalDateTime since) {
        LocalDateTime start = since != null ? since : LocalDateTime.now().minusDays(30);
        List<Object[]> rows = orderService.getDailySalesReport(start);

        List<Map<String, Object>> report = rows.stream()
                .map(row -> Map.of(
                        "orderDate", row[0],
                        "totalOrders", row[1],
                        "totalRevenue", row[2]))
                .toList();

        return ResponseEntity.ok(report);
    }
}
