package com.SpringBootProject.ExpenseTracker.entity;

import com.SpringBootProject.ExpenseTracker.entity.Category;
import com.SpringBootProject.ExpenseTracker.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String description; // e.g., "Lunch at Cafe Coffee Day"

    // -----------------------------------------------------------------------
    // WHY BigDecimal AND NOT double/float FOR MONEY?
    // -----------------------------------------------------------------------
    // float and double use binary floating-point representation internally.
    // This causes rounding errors: 0.1 + 0.2 in floating point = 0.30000000000000004
    // For money, this is UNACCEPTABLE. BigDecimal is exact decimal arithmetic.
    // precision = total digits, scale = digits after decimal point.
    // precision=10, scale=2 means: up to 99999999.99 — enough for most expenses!
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // LocalDate stores just the date (no time, no timezone) — perfect for "expense date".
    // Stored as DATE type in PostgreSQL.
    // If you needed timestamp (e.g. "created at 3:45pm"), you'd use LocalDateTime.
    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String notes; // Optional extra notes

    // -----------------------------------------------------------------------
    // MANY-TO-ONE: Many Expenses → One User
    // -----------------------------------------------------------------------
    // This is the "owning side" of the User <-> Expense relationship.
    // The foreign key column (user_id) lives ON THIS TABLE in the DB.
    //
    // @JoinColumn(name = "user_id") tells Hibernate: "the FK column is named 'user_id'"
    // Without @JoinColumn, Hibernate would auto-name it something like "user_id" anyway,
    // but being explicit is better practice.
    //
    // FetchType.LAZY here means: when you load an Expense, don't automatically run
    // a JOIN to fetch the User — load the User object lazily only if accessed.
    // FetchType.EAGER (the default for @ManyToOne) would JOIN every time, which
    // can lead to the N+1 problem (a classic interview topic!).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -----------------------------------------------------------------------
    // MANY-TO-ONE: Many Expenses → One Category
    // -----------------------------------------------------------------------
    // Same pattern. An expense belongs to one category, but a category has many expenses.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}