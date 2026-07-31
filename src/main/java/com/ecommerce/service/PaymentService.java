package com.ecommerce.service;

import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Payment;
import com.ecommerce.model.enums.OrderStatus;
import com.ecommerce.model.enums.PaymentStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * Processes payment for an order. In a real system this would call out
     * to a payment gateway (Stripe, PayPal, ...); here it's simulated but
     * the transaction boundary is what matters: the Payment record and the
     * Order's status transition together, or not at all.
     */
    @Transactional(rollbackFor = Exception.class)
    public Payment processPayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentFailedException("Order " + order.getOrderNumber() + " is not awaiting payment");
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .transactionRef(UUID.randomUUID().toString())
                .build();

        boolean success = simulateGatewayCharge(order.getTotalAmount());

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            // Order stays PENDING so the customer can retry payment.
        }

        Payment saved = paymentRepository.save(payment);

        if (!success) {
            throw new PaymentFailedException("Payment gateway declined the charge for order " + order.getOrderNumber());
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for order: " + orderId));
    }

    // Placeholder for a real payment-gateway integration.
    private boolean simulateGatewayCharge(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
