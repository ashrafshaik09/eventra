package com.atlan.evently.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for event category creation and updates.
 */
@Data
public class EventCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color code must be a valid hex color (e.g., #FF5733)")
    private String colorCode;

    @Size(max = 50, message = "Icon name cannot exceed 50 characters")
    private String iconName;

    private Boolean isActive = true;
}
