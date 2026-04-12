package com.SpringBootProject.ExpenseTracker.controller;

import com.SpringBootProject.ExpenseTracker.dto.CategorySummaryResponse;
import com.SpringBootProject.ExpenseTracker.dto.ExpenseRequest;
import com.SpringBootProject.ExpenseTracker.dto.ExpenseResponse;
import com.SpringBootProject.ExpenseTracker.dto.MonthlySummaryResponse;
import com.SpringBootProject.ExpenseTracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Track and manage expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    // -----------------------------------------------------------------------
    // POST /api/expenses
    // -----------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    // -----------------------------------------------------------------------
    // GET /api/expenses?userId=1&page=0&size=10&sort=date,desc
    // -----------------------------------------------------------------------
    // @RequestParam binds query parameters from the URL.
    // defaultValue means the param is optional — if not sent, we use the default.
    //
    // Pagination with Spring:
    // The client calls: GET /api/expenses?userId=1&page=0&size=5&sort=date,desc
    // We manually build a Pageable from those params here.
    // Alternatively, you can use @PageableDefault and Spring auto-constructs Pageable
    // from the request params — a nice shortcut once you're comfortable with pagination.
    @GetMapping
    @Operation(summary = "Get all expenses for a user (paginated)")
    public ResponseEntity<Page<ExpenseResponse>> getExpensesByUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,        // Which page (0-indexed)
            @RequestParam(defaultValue = "10") int size,       // Items per page
            @RequestParam(defaultValue = "date") String sortBy, // Which field to sort by
            @RequestParam(defaultValue = "desc") String direction  // asc or desc
    ) {
        // Build the Sort object from the string parameters
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(expenseService.getExpensesByUser(userId, pageable));
        // The Page<ExpenseResponse> object Spring serializes to JSON looks like:
        // {
        //   "content": [...],        ← the actual expenses for this page
        //   "totalElements": 47,     ← total expenses across ALL pages
        //   "totalPages": 5,
        //   "size": 10,
        //   "number": 0,             ← current page number
        //   "first": true,
        //   "last": false
        // }
        // This gives the frontend everything it needs to render a pagination control.
    }

    // -----------------------------------------------------------------------
    // GET /api/expenses/{id}
    // -----------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense by ID")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/expenses/{id}
    // -----------------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "Update an expense")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/expenses/{id}
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // ANALYTICS: GET /api/expenses/summary/by-category?userId=1
    // -----------------------------------------------------------------------
    // "/summary/by-category" is a sub-path — still under /api/expenses.
    // Having analytics under the same resource path is a clean REST design choice.
    @GetMapping("/summary/by-category")
    @Operation(summary = "Get spending breakdown by category for a user")
    public ResponseEntity<List<CategorySummaryResponse>> getSpendingByCategory(@RequestParam Long userId) {
        return ResponseEntity.ok(expenseService.getSpendingByCategory(userId));
    }

    // -----------------------------------------------------------------------
    // ANALYTICS: GET /api/expenses/summary/monthly?userId=1&year=2025&month=3
    // -----------------------------------------------------------------------
    @GetMapping("/summary/monthly")
    @Operation(summary = "Get total spending for a specific month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getMonthlySummary(userId, year, month));
    }
}