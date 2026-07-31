package com.ecommerce.model.entity;

import com.ecommerce.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A sellable product with inventory tracking.
 * Indexed on (name) for search and (category_id) for catalog browsing -
 * see V3__add_indexes.sql.
 */
@Entity
@Table(name = "products",
       indexes = {
           @Index(name = "idx_product_name", columnList = "name"),
           @Index(name = "idx_product_category", columnList = "category_id")
       })
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    // LAZY: we rarely need the full Category graph just to list products,
    // so eager loading here would be a common source of the N+1 problem.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ---------------- Business methods ----------------

    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    /** Decrements stock, guarding against overselling. Called inside a DB transaction. */
    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > stock) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + name + "' (requested " + quantity + ", available " + stock + ")");
        }
        this.stock -= quantity;
    }

    /** Restocks inventory, e.g. after an order cancellation. */
    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }
}
