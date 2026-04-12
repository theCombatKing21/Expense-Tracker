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
public class MonthlySummaryResponse {

    // Returned by the "monthly summary" analytics endpoint.
    // Tells the client: in this specific year+month, the user spent this much across N expenses.
    private int year;
    private int month;
    private BigDecimal totalSpending;
    private int expenseCount;
}