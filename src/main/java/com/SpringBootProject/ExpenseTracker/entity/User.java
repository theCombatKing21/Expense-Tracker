package com.SpringBootProject.ExpenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// @Entity tells Hibernate: "this class represents a database table"
// Hibernate will CREATE a table named "users" (derived from class name by default)
@Entity
// @Table lets you customize the actual table name.
// "users" is more conventional in PostgreSQL than "user" (which is also a reserved SQL keyword!)
@Table(name = "users")

// Lombok annotations — these get REPLACED by real code at compile time:
// @Data generates: getters, setters, equals(), hashCode(), toString()
// @Builder generates: User.builder().name("Alice").email("a@b.com").build() pattern
// @NoArgsConstructor and @AllArgsConstructor generate both constructors (JPA requires the no-arg one)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id  // This field is the PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY means the DB auto-increments the ID (SERIAL / BIGSERIAL in PostgreSQL).
    // Other strategies: SEQUENCE (uses a DB sequence), AUTO (Hibernate picks one), TABLE (slow, avoid).
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    // unique = true creates a UNIQUE CONSTRAINT in the DB — no two users can have the same email.
    private String email;

    @Column(nullable = false)
    private String password; // Will store hashed password later when JWT is added

    // -----------------------------------------------------------------------
    // ONE-TO-MANY RELATIONSHIP: One User → Many Expenses
    // -----------------------------------------------------------------------
    // mappedBy = "user" means: "the 'user' field on the Expense entity owns this relationship"
    // This tells Hibernate that the foreign key column lives on the EXPENSE table, not here.
    // Without mappedBy, Hibernate would create an unnecessary join table.
    //
    // cascade = CascadeType.ALL means: if you delete a User, all their Expenses are also deleted.
    // This is "orphan removal" at the JPA level. Think of it as cascading consequences.
    //
    // orphanRemoval = true means: if you REMOVE an expense from this list in Java code,
    // it gets deleted from the DB too (not just unlinked).
    //
    // fetch = FetchType.LAZY means: don't load expenses from DB until you actually access this list.
    // This is important for performance — you don't want to load 1000 expenses just to get a user's email.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    // @Builder.Default is needed so Lombok's @Builder initializes this list (otherwise it'd be null)
    private List<Expense> expenses = new ArrayList<>();
}

