package com.ecommerce;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.dto.OrderDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.entity.User;
import com.ecommerce.model.enums.Role;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests proving the transactional guarantee described in
 * OrderService: an order that fails partway through (e.g. insufficient
 * stock on its second item) must roll back stock changes from earlier
 * items in the same request, not just fail without side effects.
 *
 * Run against an in-memory H2 database (see application-test.yml) so
 * tests don't require a running PostgreSQL instance.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;
    private Long productAId;
    private Long productBId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.builder().name("Test Category").build());

        Product productA = productRepository.save(Product.builder()
                .name("Product A").price(BigDecimal.TEN).stock(5).category(category).isActive(true).build());
        Product productB = productRepository.save(Product.builder()
                .name("Product B").price(BigDecimal.valueOf(20)).stock(1).category(category).isActive(true).build());

        User user = userRepository.save(User.builder()
                .username("tester").email("tester@example.com").password("hashed").role(Role.CUSTOMER).enabled(true)
                .build());

        userId = user.getId();
        productAId = productA.getId();
        productBId = productB.getId();
    }

    @Test
    @Transactional
    void createOrder_succeeds_andDecrementsStockForAllItems() {
        OrderDTO.CreateOrderRequest request = OrderDTO.CreateOrderRequest.builder()
                .userId(userId)
                .items(List.of(
                        new OrderDTO.CreateOrderItemRequest(productAId, 2),
                        new OrderDTO.CreateOrderItemRequest(productBId, 1)))
                .shippingAddress("123 Test St")
                .build();

        OrderDTO created = orderService.createOrder(request);

        assertThat(created.getTotalAmount()).isEqualByComparingTo("40.00");
        assertThat(productRepository.findById(productAId).orElseThrow().getStock()).isEqualTo(3);
        assertThat(productRepository.findById(productBId).orElseThrow().getStock()).isEqualTo(0);
    }

    @Test
    @Transactional
    void createOrder_rollsBackEntireOrder_whenAnyItemHasInsufficientStock() {
        // Product B only has 1 in stock; requesting 5 should fail and roll back
        // the stock already decremented for Product A earlier in the loop.
        OrderDTO.CreateOrderRequest request = OrderDTO.CreateOrderRequest.builder()
                .userId(userId)
                .items(List.of(
                        new OrderDTO.CreateOrderItemRequest(productAId, 2),
                        new OrderDTO.CreateOrderItemRequest(productBId, 5)))
                .shippingAddress("123 Test St")
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);

        // Because the whole method is @Transactional, product A's stock change
        // must NOT be persisted once the surrounding transaction rolls back.
        assertThat(productRepository.findById(productAId).orElseThrow().getStock()).isEqualTo(5);
    }
}
