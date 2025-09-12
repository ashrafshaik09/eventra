package com.atlan.evently.controller;

import com.atlan.evently.dto.EventCategoryRequest;
import com.atlan.evently.dto.EventCategoryResponse;
import com.atlan.evently.service.EventCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for event category management.
 * Provides CRUD operations and category-based event filtering.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Event Categories", description = "Event category management and filtering API")
public class EventCategoryController {

    private final EventCategoryService eventCategoryService;

    @GetMapping
    @Operation(summary = "Get all active categories", description = "Retrieve all active event categories for filtering")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    public ResponseEntity<List<EventCategoryResponse>> getActiveCategories() {
        List<EventCategoryResponse> categories = eventCategoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories (paginated)", description = "Retrieve all categories with pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated categories")
    public ResponseEntity<Page<EventCategoryResponse>> getAllCategories(Pageable pageable) {
        Page<EventCategoryResponse> categories = eventCategoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieve detailed information about a specific category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<EventCategoryResponse> getCategoryById(
            @Parameter(description = "Category UUID") @PathVariable String categoryId) {
        EventCategoryResponse category = eventCategoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching categories")
    public ResponseEntity<List<EventCategoryResponse>> searchCategories(
            @Parameter(description = "Search term") @RequestParam String q) {
        List<EventCategoryResponse> categories = eventCategoryService.searchCategories(q);
        return ResponseEntity.ok(categories);
    }

    // Admin endpoints for category management
    @PostMapping
    @Operation(summary = "Create category (Admin)", description = "Create a new event category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category data"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<EventCategoryResponse> createCategory(@Valid @RequestBody EventCategoryRequest request) {
        EventCategoryResponse category = eventCategoryService.createCategory(request);
        return ResponseEntity.status(201).body(category);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category (Admin)", description = "Update an existing event category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category data"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<EventCategoryResponse> updateCategory(
            @Parameter(description = "Category UUID") @PathVariable String categoryId,
            @Valid @RequestBody EventCategoryRequest request) {
        EventCategoryResponse category = eventCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category (Admin)", description = "Soft delete an event category")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category UUID") @PathVariable String categoryId) {
        eventCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
