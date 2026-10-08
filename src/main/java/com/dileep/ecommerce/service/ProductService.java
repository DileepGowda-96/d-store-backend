package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.ProductRequest;
import com.dileep.ecommerce.dto.ProductResponse;
import com.dileep.ecommerce.entity.Category;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.CategoryRepository;
import com.dileep.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Category not found"
                ));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        return convertToResponse(
                productRepository.save(product)
        );
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        return convertToResponse(product);
    }

    public ProductResponse updateProduct(
            Long id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Category not found"
                ));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return convertToResponse(
                productRepository.save(product)
        );
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        productRepository.delete(product);
    }

    public List<ProductResponse> searchProducts(String name) {

        return productRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByCategory(
            Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(
                    "Category not found"
            );
        }

        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository
                .findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private ProductResponse convertToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}