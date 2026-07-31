package com.ecommerce.repository;

import com.ecommerce.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Root categories (no parent) - used to render the top level of the tree
    List<Category> findByParentIsNull();

    // Children of a given category
    List<Category> findByParentId(Long parentId);
}
