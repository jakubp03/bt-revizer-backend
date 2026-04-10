package com.opr3.opr3.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.category.CategoryResponse;
import com.opr3.opr3.dto.category.CreateCategoryRequest;
import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.exception.InvalidRequestException;
import com.opr3.opr3.exception.ResourceAlreadyExistsException;
import com.opr3.opr3.repository.CategoryRepository;
import com.opr3.opr3.service.auth.AuthUtilService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final AuthUtilService authUtilService;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        User user = authUtilService.getAuthenticatedUser();

        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidRequestException("Category name must not be blank");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndUserUid(request.getName(), user.getUid())) {
            throw new ResourceAlreadyExistsException(
                    "Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName().trim())
                .description(request.getDescription())
                .color(request.getColor())
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getUserCategories() {
        User user = authUtilService.getAuthenticatedUser();

        List<CategoryResponse> categories = categoryRepository
                .findByUserUidOrderByNameAsc(user.getUid())
                .stream()
                .map(CategoryResponse::from)
                .toList();

        log.info("Returning {} categories for user '{}'", categories.size(), user.getUid());

        return categories;
    }
}
