package com.SpringBootProject.ExpenseTracker.dto;

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
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String notes;

    // Instead of returning raw foreign key IDs only, we return both
    // the ID and the human-readable name — much more useful for any frontend
    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
}