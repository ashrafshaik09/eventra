package com.atlan.evently.service;

import com.atlan.evently.dto.EventCategoryRequest;
import com.atlan.evently.dto.EventCategoryResponse;
import com.atlan.evently.exception.EventException;
import com.atlan.evently.mapper.EventCategoryMapper;
import com.atlan.evently.model.EventCategory;
import com.atlan.evently.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing event categories with caching and validation.
 * Supports category hierarchy and usage analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;
    private final EventCategoryMapper eventCategoryMapper;

    @Cacheable(value = "categories", key = "'active'")
    @Transactional(readOnly = true)
    public List<EventCategoryResponse> getActiveCategories() {
        log.debug("Cache miss - fetching active categories from database");
        return eventCategoryRepository.findByIsActiveTrueOrderByName()
                .stream()
                .map(eventCategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EventCategoryResponse> getAllCategories(Pageable pageable) {
        return eventCategoryRepository.findByIsActiveTrue(pageable)
                .map(eventCategoryMapper::toResponse);
    }

    @Cacheable(value = "category-details", key = "#categoryId")
    @Transactional(readOnly = true)
    public EventCategoryResponse getCategoryById(String categoryId) {
        UUID uuid = parseUUID(categoryId, "Category ID");
        EventCategory category = eventCategoryRepository.findById(uuid)
                .orElseThrow(() -> new EventException("Category not found",
                        "CATEGORY_NOT_FOUND",
                        "Category with ID " + categoryId + " does not exist"));
        return eventCategoryMapper.toResponse(category);
    }

    @CacheEvict(value = {"categories", "category-details"}, allEntries = true)
    @Transactional
    public EventCategoryResponse createCategory(EventCategoryRequest request) {
        validateCategoryRequest(request);
        
        if (eventCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new EventException("Category name already exists",
                    "CATEGORY_NAME_EXISTS",
                    "A category with name '" + request.getName() + "' already exists");
        }

        EventCategory category = eventCategoryMapper.toEntity(request);
        EventCategory savedCategory = eventCategoryRepository.save(category);
        
        log.info("Created new category: {} with ID: {}", request.getName(), savedCategory.getId());
        return eventCategoryMapper.toResponse(savedCategory);
    }

    @CacheEvict(value = {"categories", "category-details"}, allEntries = true)
    @Transactional
    public EventCategoryResponse updateCategory(String categoryId, EventCategoryRequest request) {
        validateCategoryRequest(request);
        UUID uuid = parseUUID(categoryId, "Category ID");
        
        EventCategory existingCategory = eventCategoryRepository.findById(uuid)
                .orElseThrow(() -> new EventException("Category not found",
                        "CATEGORY_NOT_FOUND",
                        "Category with ID " + categoryId + " does not exist"));

        // Check name uniqueness if changed
        if (!existingCategory.getName().equalsIgnoreCase(request.getName()) &&
            eventCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new EventException("Category name already exists",
                    "CATEGORY_NAME_EXISTS",
                    "A category with name '" + request.getName() + "' already exists");
        }

        eventCategoryMapper.updateEntity(existingCategory, request);
        EventCategory savedCategory = eventCategoryRepository.save(existingCategory);
        
        log.info("Updated category: {} with ID: {}", request.getName(), categoryId);
        return eventCategoryMapper.toResponse(savedCategory);
    }

    @CacheEvict(value = {"categories", "category-details"}, allEntries = true)
    @Transactional
    public void deleteCategory(String categoryId) {
        UUID uuid = parseUUID(categoryId, "Category ID");
        EventCategory category = eventCategoryRepository.findById(uuid)
                .orElseThrow(() -> new EventException("Category not found",
                        "CATEGORY_NOT_FOUND",
                        "Category with ID " + categoryId + " does not exist"));

        // Soft delete - mark as inactive instead of hard delete
        category.setIsActive(false);
        eventCategoryRepository.save(category);
        
        log.info("Soft deleted category: {} with ID: {}", category.getName(), categoryId);
    }

    @Transactional(readOnly = true)
    public List<EventCategoryResponse> searchCategories(String searchTerm) {
        return eventCategoryRepository.findActiveByNameContaining(searchTerm)
                .stream()
                .map(eventCategoryMapper::toResponse)
                .toList();
    }

    private void validateCategoryRequest(EventCategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (request.getName().length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        }
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}
