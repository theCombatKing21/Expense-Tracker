package com.SpringBootProject.ExpenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be 255 characters or fewer")
    private String description;

    // @NotNull (not @NotBlank) because BigDecimal is not a String
    // @Positive ensures amount is strictly greater than 0
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive value")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate date;

    private String notes; // optional

    // The client sends IDs — the Service layer resolves them to full entities
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}