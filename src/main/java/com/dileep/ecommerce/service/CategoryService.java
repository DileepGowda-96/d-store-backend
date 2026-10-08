package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.CategoryRequest;
import com.dileep.ecommerce.dto.CategoryResponse;
import com.dileep.ecommerce.entity.Category;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException(
                    "Category already exists"
            );
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return convertToResponse(
                categoryRepository.save(category)
        );
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        ));

        return convertToResponse(category);
    }

    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        ));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return convertToResponse(
                categoryRepository.save(category)
        );
    }

    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        ));

        categoryRepository.delete(category);
    }

    private CategoryResponse convertToResponse(
            Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}