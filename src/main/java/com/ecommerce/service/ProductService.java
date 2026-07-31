package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.dto.ProductDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Cached because the catalog is read far more often than it's written.
     * Cache key includes all filter params so different queries don't collide.
     */
    @Cacheable(value = "products", key = "#categoryId + '-' + #minPrice + '-' + #maxPrice + '-' + #pageable")
    @Transactional(readOnly = true)
    public Page<ProductDTO> filterProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.filterProducts(categoryId, minPrice, maxPrice, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toDTO(product);
    }

    // Evicting the whole cache on writes is simplest and safe for a catalog
    // of this size; a finer-grained eviction strategy could target only the
    // affected category if the catalog grows very large.
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductDTO create(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(category)
                .imageUrl(dto.getImageUrl())
                .isActive(dto.getIsActive() == null || dto.getIsActive())
                .build();

        return toDTO(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }

        return toDTO(product); // dirty checking flushes changes at commit, no explicit save() needed
    }

    // Soft delete: keeps historical order_items referencing this product valid.
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setIsActive(false);
    }

    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .imageUrl(p.getImageUrl())
                .isActive(p.getIsActive())
                .build();
    }
}
