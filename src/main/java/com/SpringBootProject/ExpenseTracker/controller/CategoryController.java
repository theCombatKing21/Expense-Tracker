package com.SpringBootProject.ExpenseTracker.controller;

import com.SpringBootProject.ExpenseTracker.dto.CategoryRequest;
import com.SpringBootProject.ExpenseTracker.dto.CategoryResponse;
import com.SpringBootProject.ExpenseTracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = @Controller + @ResponseBody
// @Controller alone means: "this handles HTTP requests"
// @ResponseBody means: "serialize the return value to JSON automatically" (via Jackson)
// @RestController combines both — ideal for REST APIs where every method returns JSON.
//
// @RequestMapping defines the BASE PATH for all endpoints in this class.
// So getAll() at @GetMapping will be at: GET /api/categories
//    getById() at @GetMapping("/{id}") will be at: GET /api/categories/5  etc.
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
// Swagger/OpenAPI annotation: groups all endpoints in this controller under "Categories" in the Swagger UI
@Tag(name = "Categories", description = "Manage expense categories")
public class CategoryController {

    private final CategoryService categoryService;

    // -----------------------------------------------------------------------
    // POST /api/categories
    // -----------------------------------------------------------------------
    // @RequestBody: tells Spring to deserialize the incoming JSON body into a CategoryRequest object.
    // @Valid: triggers the validation annotations (@NotBlank, @Size etc.) on the DTO.
    //         If validation fails, MethodArgumentNotValidException is thrown automatically,
    //         which our GlobalExceptionHandler catches and returns a 400.
    // ResponseEntity<T>: gives you full control over HTTP status code + response body.
    //   ResponseEntity.status(HttpStatus.CREATED).body(response) → 201 Created + JSON body
    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // 201 CREATED (not 200 OK) is the correct status for a successful resource creation.
        // This is a REST convention — use the right status code, it communicates meaning.
    }

    // -----------------------------------------------------------------------
    // GET /api/categories
    // -----------------------------------------------------------------------
    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
        // ResponseEntity.ok() is shorthand for ResponseEntity.status(200).body(...)
    }

    // -----------------------------------------------------------------------
    // GET /api/categories/{id}
    // -----------------------------------------------------------------------
    // @PathVariable: binds the {id} part of the URL to the method parameter.
    // GET /api/categories/5  →  id = 5L
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/categories/{id}
    // -----------------------------------------------------------------------
    // PUT = full replacement of a resource (you send ALL fields).
    // PATCH = partial update (you send only changed fields). We keep it simple with PUT for now.
    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/categories/{id}
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        // 204 No Content: successful operation, but nothing to return in the body.
        // This is the correct REST status for a successful DELETE.
        return ResponseEntity.noContent().build();
    }
}