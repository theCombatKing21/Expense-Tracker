package com.SpringBootProject.ExpenseTracker.repository;

import com.SpringBootProject.ExpenseTracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Generates: SELECT * FROM categories WHERE name = ?
    // Useful for checking if a category with that name already exists.
    Optional<Category> findByName(String name);

    // Generates: SELECT COUNT(*) > 0 FROM categories WHERE name = ?
    boolean existsByName(String name);
}