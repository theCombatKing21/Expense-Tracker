package com.SpringBootProject.ExpenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // e.g., "Food", "Transport", "Entertainment"

    @Column(length = 255)
    private String description; // e.g., "All food and dining expenses"

    // -----------------------------------------------------------------------
    // ONE-TO-MANY: One Category → Many Expenses
    // -----------------------------------------------------------------------
    // Same pattern as User → Expenses.
    // CascadeType.ALL would mean deleting a Category deletes all its expenses.
    // Think carefully: is that the right behaviour for your app?
    // For now yes — if someone deletes "Food" category, associated expenses go too.
    // You could also use CascadeType.PERSIST, MERGE only and handle deletes manually.
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();
}