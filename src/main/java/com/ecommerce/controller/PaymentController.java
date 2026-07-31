package com.ecommerce.controller;

import com.ecommerce.model.entity.Payment;
import com.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/{orderId}?method=CREDIT_CARD
    @PostMapping("/{orderId}")
    public ResponseEntity<Payment> pay(@PathVariable Long orderId, @RequestParam String method) {
        return ResponseEntity.ok(paymentService.processPayment(orderId, method));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }
}
