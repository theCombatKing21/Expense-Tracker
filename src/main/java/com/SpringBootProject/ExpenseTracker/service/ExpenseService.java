package com.SpringBootProject.ExpenseTracker.service;

import com.SpringBootProject.ExpenseTracker.dto.CategorySummaryResponse;
import com.SpringBootProject.ExpenseTracker.dto.ExpenseRequest;
import com.SpringBootProject.ExpenseTracker.dto.ExpenseResponse;
import com.SpringBootProject.ExpenseTracker.dto.MonthlySummaryResponse;
import com.SpringBootProject.ExpenseTracker.entity.Category;
import com.SpringBootProject.ExpenseTracker.entity.Expense;
import com.SpringBootProject.ExpenseTracker.entity.User;
import com.SpringBootProject.ExpenseTracker.exception.ResourceNotFoundException;
import com.SpringBootProject.ExpenseTracker.repository.CategoryRepository;
import com.SpringBootProject.ExpenseTracker.repository.ExpenseRepository;
import com.SpringBootProject.ExpenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        // The client sends us a userId and categoryId.
        // We must look up the actual entities — JPA relationships need real objects, not just IDs.
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)           // Full entity objects, not just IDs
                .category(category)   // Full entity objects, not just IDs
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // READ ALL FOR A USER (PAGINATED)
    // -----------------------------------------------------------------------
    // Pageable is passed in from the controller. It contains: page number, page size, sort.
    // The caller constructs it like: PageRequest.of(0, 10, Sort.by("date").descending())
    // Page<ExpenseResponse> gives the client: items, totalElements, totalPages, isLast etc.
    // This is critical for any list that could grow large — never return everything at once.
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByUser(Long userId, Pageable pageable) {
        // Verify user exists first
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        // findByUserId with Pageable returns a Page<Expense>, we map each to a DTO
        return expenseRepository.findByUserId(userId, pageable)
                .map(this::toResponse);  // Page has its own .map() — very convenient
    }

    // -----------------------------------------------------------------------
    // READ SINGLE
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return toResponse(expense);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        existing.setDescription(request.getDescription());
        existing.setAmount(request.getAmount());
        existing.setDate(request.getDate());
        existing.setNotes(request.getNotes());
        existing.setUser(user);
        existing.setCategory(category);

        Expense updated = expenseRepository.save(existing);
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------
    // ANALYTICS: Spending by Category
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getSpendingByCategory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // This returns List<Object[]> from our custom JPQL query.
        // Each Object[] is: [0] = categoryName (String), [1] = totalAmount (BigDecimal)
        List<Object[]> results = expenseRepository.findSpendingByCategory(userId);

        // We map each raw Object[] into a clean DTO
        return results.stream()
                .map(row -> CategorySummaryResponse.builder()
                        .categoryName((String) row[0])
                        .totalAmount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // ANALYTICS: Monthly Summary
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long userId, int year, int month) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Build the date range for the requested month dynamically
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // Last day of month

        BigDecimal total = expenseRepository.findTotalSpendingBetweenDates(userId, startDate, endDate);

        // Count how many individual expenses in that range
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        return MonthlySummaryResponse.builder()
                .year(year)
                .month(month)
                .totalSpending(total)
                .expenseCount(expenses.size())
                .build();
    }

    // -----------------------------------------------------------------------
    // PRIVATE: Entity → DTO
    // -----------------------------------------------------------------------
    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .notes(expense.getNotes())
                .userId(expense.getUser().getId())
                .userName(expense.getUser().getName())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .build();
    }
}