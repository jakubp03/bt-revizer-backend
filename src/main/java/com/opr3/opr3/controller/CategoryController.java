package com.opr3.opr3.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.category.CategoryResponse;
import com.opr3.opr3.dto.category.CreateCategoryRequest;
import com.opr3.opr3.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        log.info("[201] created category ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getUserCategories() {
        List<CategoryResponse> categories = categoryService.getUserCategories();
        log.info("[200] returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }
}
