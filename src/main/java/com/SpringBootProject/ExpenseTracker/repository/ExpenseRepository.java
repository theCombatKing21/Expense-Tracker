package com.SpringBootProject.ExpenseTracker.repository;
import com.SpringBootProject.ExpenseTracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // -----------------------------------------------------------------------
    // DERIVED QUERY METHODS — Spring reads the method name, generates the SQL
    // -----------------------------------------------------------------------

    // SELECT * FROM expenses WHERE user_id = ?
    List<Expense> findByUserId(Long userId);

    // SELECT * FROM expenses WHERE user_id = ? AND category_id = ?
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // SELECT * FROM expenses WHERE user_id = ? AND date BETWEEN ? AND ?
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // -----------------------------------------------------------------------
    // PAGINATION — a very common real-world and interview topic
    // -----------------------------------------------------------------------
    // The Pageable parameter tells Spring to add LIMIT and OFFSET to the query.
    // The caller creates: PageRequest.of(pageNumber, pageSize, Sort.by("date").descending())
    // The return type Page<Expense> contains: the items, total count, total pages, etc.
    // This is how production apps handle large datasets — you never load ALL rows.
    Page<Expense> findByUserId(Long userId, Pageable pageable);

    // -----------------------------------------------------------------------
    // CUSTOM JPQL QUERIES — when derived methods aren't enough
    // -----------------------------------------------------------------------
    // JPQL (Java Persistence Query Language) looks like SQL but uses ENTITY CLASS
    // names and FIELD names — NOT table names and column names.
    // Hibernate then translates this to actual PostgreSQL SQL at runtime.
    //
    // Here we calculate total spending per category for a given user.
    // SUM(e.amount) is an aggregate function. We GROUP BY category name.
    // The result is a List of Object arrays: each array has [categoryName, totalAmount].
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category.name")
    List<Object[]> findSpendingByCategory(@Param("userId") Long userId);

    // Total spending for a user in a specific date range — used for monthly summary API.
    // COALESCE(SUM(...), 0) returns 0 if there are no expenses (instead of null).
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal findTotalSpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}