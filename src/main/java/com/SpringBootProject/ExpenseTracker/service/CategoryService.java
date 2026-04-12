package com.SpringBootProject.ExpenseTracker.service;

import com.SpringBootProject.ExpenseTracker.dto.CategoryRequest;
import com.SpringBootProject.ExpenseTracker.dto.CategoryResponse;
import com.SpringBootProject.ExpenseTracker.entity.Category;
import com.SpringBootProject.ExpenseTracker.exception.DuplicateResourceException;
import com.SpringBootProject.ExpenseTracker.exception.ResourceNotFoundException;
import com.SpringBootProject.ExpenseTracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// @Service is semantically the same as @Component — it registers this class as a Spring bean.
// The difference is purely communicative: @Service signals "this class contains business logic".
// It's part of making your code self-documenting.
//
// @RequiredArgsConstructor (Lombok) generates a constructor that takes all FINAL fields as params.
// Spring then uses that constructor to inject the CategoryRepository dependency.
// This is "constructor injection" — the preferred style over @Autowired field injection
// because it makes dependencies explicit and makes the class easier to unit test.
@Service
@RequiredArgsConstructor
public class CategoryService {

    // 'final' forces Lombok's @RequiredArgsConstructor to include this in the constructor.
    // The injected repository is immutable after construction — good practice.
    private final CategoryRepository categoryRepository;

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    // @Transactional means: wrap this entire method in a database transaction.
    // If any line throws an exception, the entire transaction ROLLS BACK — no partial saves.
    // This is critical for data consistency. On @Service methods that write to DB, always use it.
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        // Business rule: category names must be unique.
        // We check BEFORE trying to save — this gives a cleaner error than a DB constraint violation.
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Category with name '" + request.getName() + "' already exists"
            );
        }

        // Build Entity from the incoming DTO.
        // Note: we don't accept an 'id' in the request — the DB generates it.
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // save() does an INSERT if the entity has no ID yet, or UPDATE if it does.
        // After save(), the returned entity has the DB-generated ID populated.
        Category saved = categoryRepository.save(category);

        // Convert the saved Entity to a Response DTO before returning.
        // We NEVER return the raw Entity from the controller — DTO is the boundary.
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // READ ALL
    // -----------------------------------------------------------------------
    // @Transactional(readOnly = true) is an optimization hint to Hibernate and the DB.
    // Hibernate skips "dirty checking" (checking if entities changed), and the DB
    // can use a read replica if your setup has one. Always use it on read-only methods.
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)   // 'this::toResponse' is a method reference — same as c -> toResponse(c)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // READ ONE
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        // findById returns Optional<Category> — we either get it or throw our custom exception.
        // .orElseThrow() is the clean Java 8+ way to handle the "not found" case.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toResponse(category);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check name uniqueness only if the name is actually changing.
        // If someone sends the same name, we don't want to falsely report a duplicate.
        if (!existing.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Category with name '" + request.getName() + "' already exists"
            );
        }

        // Update the fields on the existing managed entity.
        // Because we're inside a @Transactional method, Hibernate is "watching" this entity.
        // When the method ends and the transaction commits, Hibernate automatically runs
        // an UPDATE SQL for any changed fields — this is called "dirty checking".
        // You don't even need to call save() again! (Though calling it is also harmless.)
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        // Explicit save() call here to be clear, though Hibernate would UPDATE anyway via dirty checking.
        Category updated = categoryRepository.save(existing);
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @Transactional
    public void deleteCategory(Long id) {
        // First verify it exists — deleteById() won't throw if the ID doesn't exist.
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------
    // PRIVATE HELPER: Entity → Response DTO mapping
    // -----------------------------------------------------------------------
    // Keeping this private and centralized is important.
    // If your response structure changes, you update ONE place.
    // In larger projects, you'd use MapStruct (an annotation-based mapper) to auto-generate this.
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}