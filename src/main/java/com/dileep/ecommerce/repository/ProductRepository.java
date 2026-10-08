package com.dileep.ecommerce.repository;

import com.dileep.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByPriceBetween(
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice
    );
}