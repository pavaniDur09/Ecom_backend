package com.ecommerce.repository;

import com.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ---------- Derived query methods ----------

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    List<Product> findByStockLessThan(Integer threshold);

    // ---------- JPQL with JOIN FETCH to avoid N+1 when the category is needed ----------

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    // ---------- Flexible filtering across multiple optional criteria ----------

    @Query("""
           SELECT p FROM Product p
           WHERE p.isActive = true
             AND (:categoryId IS NULL OR p.category.id = :categoryId)
             AND (:minPrice IS NULL OR p.price >= :minPrice)
             AND (:maxPrice IS NULL OR p.price <= :maxPrice)
           """)
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  Pageable pageable);

    // ---------- Native query example: top selling products ----------

    @Query(value = """
        SELECT p.id, p.name, SUM(oi.quantity) AS total_sold
        FROM products p
        JOIN order_items oi ON oi.product_id = p.id
        GROUP BY p.id, p.name
        ORDER BY total_sold DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("limit") int limit);
}
