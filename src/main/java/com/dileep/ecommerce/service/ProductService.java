package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.ProductRequest;
import com.dileep.ecommerce.dto.ProductResponse;
import com.dileep.ecommerce.entity.Category;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.CategoryRepository;
import com.dileep.ecommerce.repository.ProductRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Create a single product
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        return convertToResponse(productRepository.save(product));
    }

    // Get all products
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get a product by ID
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        return convertToResponse(product);
    }

    // Update a product
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return convertToResponse(productRepository.save(product));
    }

    // Delete a product
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    // Search products by name
    public List<ProductResponse> searchProducts(String name) {

        return productRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get products by category
    public List<ProductResponse> getProductsByCategory(
            Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get products within a price range
    public List<ProductResponse> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository
                .findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Bulk upload products from a CSV file
    @Transactional
    public Map<String, Object> bulkUploadProducts(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a non-empty CSV file."
            );
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "CSV file must not exceed 5 MB."
            );
        }

        List<Product> productsToSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        Map<Long, Category> categoryCache = new HashMap<>();

        int rowNumber = 1;

        try (
                Reader reader = new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                );

                CSVParser csvParser = CSVFormat.RFC4180.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .get()
                        .parse(reader)
        ) {

            Map<String, Integer> headers = csvParser.getHeaderMap();

            if (headers == null ||
                    !headers.keySet().containsAll(List.of(
                            "name",
                            "description",
                            "price",
                            "stockQuantity",
                            "imageUrl",
                            "categoryId"
                    ))) {

                throw new IllegalArgumentException(
                        "CSV must contain these headers: " +
                                "name,description,price,stockQuantity,imageUrl,categoryId"
                );
            }

            for (CSVRecord record : csvParser) {

                rowNumber++;

                if (rowNumber > 1002) {
                    errors.add(
                            "CSV exceeds the maximum of 1000 product rows."
                    );
                    break;
                }

                try {

                    String name = record.get("name").trim();
                    String description =
                            record.get("description").trim();
                    String priceText = record.get("price").trim();
                    String stockText =
                            record.get("stockQuantity").trim();
                    String imageUrl = record.get("imageUrl").trim();
                    String categoryText =
                            record.get("categoryId").trim();

                    if (name.isBlank()) {
                        throw new IllegalArgumentException(
                                "Product name is required."
                        );
                    }

                    if (name.length() > 150) {
                        throw new IllegalArgumentException(
                                "Product name cannot exceed 150 characters."
                        );
                    }

                    if (description.length() > 2000) {
                        throw new IllegalArgumentException(
                                "Description cannot exceed 2000 characters."
                        );
                    }

                    if (imageUrl.length() > 255) {
                        throw new IllegalArgumentException(
                                "Image URL cannot exceed 255 characters."
                        );
                    }

                    if (priceText.isBlank()) {
                        throw new IllegalArgumentException(
                                "Price is required."
                        );
                    }

                    BigDecimal price = new BigDecimal(priceText);

                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                "Price must be greater than zero."
                        );
                    }

                    if (price.scale() > 2 ||
                            price.precision() > 10) {
                        throw new IllegalArgumentException(
                                "Price must fit the database precision of 10 digits and 2 decimal places."
                        );
                    }

                    if (stockText.isBlank()) {
                        throw new IllegalArgumentException(
                                "Stock quantity is required."
                        );
                    }

                    int stockQuantity = Integer.parseInt(stockText);

                    if (stockQuantity < 0) {
                        throw new IllegalArgumentException(
                                "Stock quantity cannot be negative."
                        );
                    }

                    if (categoryText.isBlank()) {
                        throw new IllegalArgumentException(
                                "Category ID is required."
                        );
                    }

                    long categoryId = Long.parseLong(categoryText);

                    if (categoryId <= 0) {
                        throw new IllegalArgumentException(
                                "Category ID must be a positive number."
                        );
                    }

                    Category category = categoryCache.get(categoryId);

                    if (category == null) {

                        category = categoryRepository.findById(categoryId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Category ID " +
                                                        categoryId +
                                                        " does not exist."
                                        ));

                        categoryCache.put(categoryId, category);
                    }

                    Product product = Product.builder()
                            .name(name)
                            .description(description)
                            .price(price)
                            .stockQuantity(stockQuantity)
                            .imageUrl(imageUrl.isBlank() ? null : imageUrl)
                            .category(category)
                            .build();

                    productsToSave.add(product);

                } catch (NumberFormatException exception) {

                    errors.add(
                            "Row " + rowNumber +
                                    ": Price, stock quantity, or category ID has an invalid number."
                    );

                } catch (IllegalArgumentException exception) {

                    errors.add(
                            "Row " + rowNumber + ": " +
                                    exception.getMessage()
                    );
                }
            }

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "Unable to read the CSV file."
            );
        }

        // Save nothing if any row is invalid.
        if (!errors.isEmpty()) {

            Map<String, Object> result = new HashMap<>();

            result.put("success", false);
            result.put("message",
                    "Upload rejected. Correct the CSV errors and try again.");
            result.put("uploadedCount", 0);
            result.put("errors", errors);

            return result;
        }

        if (productsToSave.isEmpty()) {
            throw new IllegalArgumentException(
                    "The CSV file contains no product rows."
            );
        }

        List<Product> savedProducts =
                productRepository.saveAll(productsToSave);

        Map<String, Object> result = new HashMap<>();

        result.put("success", true);
        result.put("message", "Products uploaded successfully.");
        result.put("uploadedCount", savedProducts.size());

        return result;
    }

    // Convert entity into API response
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