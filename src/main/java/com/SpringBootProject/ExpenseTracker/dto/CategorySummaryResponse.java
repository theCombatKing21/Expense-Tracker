package com.SpringBootProject.ExpenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryResponse {

    // Each instance of this DTO represents one row of the analytics result:
    // "How much did the user spend in this category in total?"
    private String categoryName;
    private BigDecimal totalAmount;
}