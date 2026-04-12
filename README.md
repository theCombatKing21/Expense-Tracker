# 💰 Expense Tracker — Spring Boot Backend

A RESTful expense tracking backend built with **Java 21**, **Spring Boot 3.4.4**, **Spring Data JPA**, **Hibernate**, and **PostgreSQL**. Designed with clean layered architecture, SOLID principles, and production-ready patterns including pagination, validation, analytics APIs, and centralised exception handling.

> **Personal project built to demonstrate hands-on Spring Boot skills** — clean architecture, JPA relationships, custom JPQL queries, DTO mapping, and REST best practices.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Validation | Spring Boot Validation (`jakarta.validation`) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Boilerplate reduction | Lombok |
| Build Tool | Maven |

---

## 📁 Project Structure

```
src/main/java/com/springbootproject/expensetracker/
│
├── ExpenseTrackerApplication.java       ← App entry point (@SpringBootApplication)
│
├── entity/                              ← JPA entities (DB table mappings)
│   ├── User.java
│   ├── Category.java
│   └── Expense.java
│
├── repository/                          ← Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   └── ExpenseRepository.java
│
├── dto/                                 ← Request & Response DTOs (API contract)
│   ├── UserRequest.java / UserResponse.java
│   ├── CategoryRequest.java / CategoryResponse.java
│   ├── ExpenseRequest.java / ExpenseResponse.java
│   ├── CategorySummaryResponse.java
│   └── MonthlySummaryResponse.java
│
├── service/                             ← Business logic layer
│   ├── UserService.java
│   ├── CategoryService.java
│   └── ExpenseService.java
│
├── controller/                          ← REST controllers (HTTP layer)
│   ├── UserController.java
│   ├── CategoryController.java
│   └── ExpenseController.java
│
└── exception/                           ← Custom exceptions + global handler
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    └── GlobalExceptionHandler.java

src/main/resources/
└── application.properties               ← DB config, JPA settings, Swagger paths
```

---

## 🏗️ Architecture & Request Flow

Every HTTP request travels through four layers in sequence. Each layer has a single, clearly defined responsibility — this is the Single Responsibility Principle in practice.

```
                        ┌─────────────────────────────────────────┐
                        │             CLIENT / SWAGGER UI          │
                        │         (HTTP Request with JSON body)    │
                        └───────────────────┬─────────────────────┘
                                            │
                                            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CONTROLLER LAYER                                │
│  • Receives HTTP request (@GetMapping, @PostMapping etc.)               │
│  • Validates request body with @Valid                                   │
│  • Calls the appropriate Service method                                 │
│  • Wraps result in ResponseEntity with correct HTTP status code         │
│                                                                         │
│   UserController     CategoryController     ExpenseController           │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          SERVICE LAYER                                  │
│  • Contains all business logic                                          │
│  • Enforces business rules (e.g. no duplicate emails, positive amounts) │
│  • Converts Request DTOs → Entities (before saving)                    │
│  • Converts Entities → Response DTOs (before returning)                │
│  • Annotated with @Transactional for data consistency                   │
│                                                                         │
│     UserService        CategoryService        ExpenseService            │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        REPOSITORY LAYER                                 │
│  • Extends JpaRepository — free CRUD operations out of the box          │
│  • Derived query methods (Spring reads method name → generates SQL)     │
│  • Custom @Query JPQL for analytics (SUM, GROUP BY)                     │
│  • Pagination via Pageable parameter                                    │
│                                                                         │
│  UserRepository    CategoryRepository    ExpenseRepository              │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER                                  │
│                     PostgreSQL (expensetracker DB)                      │
│                                                                         │
│   Table: users        Table: categories       Table: expenses           │
│   id, name,           id, name,               id, description,          │
│   email, password     description             amount, date, notes,      │
│                                               user_id (FK),             │
│                                               category_id (FK)          │
└─────────────────────────────────────────────────────────────────────────┘
```

If any layer throws an exception, the `GlobalExceptionHandler` (@RestControllerAdvice) intercepts it before it reaches the client and returns a structured JSON error response.

---

## 🗃️ Data Model & Relationships

```
┌──────────────┐          ┌──────────────────┐          ┌──────────────┐
│     USER     │          │     EXPENSE       │          │   CATEGORY   │
│──────────────│          │──────────────────│          │──────────────│
│ id (PK)      │◄────┐    │ id (PK)          │    ┌────►│ id (PK)      │
│ name         │     │    │ description      │    │     │ name         │
│ email        │     │    │ amount (Decimal) │    │     │ description  │
│ password     │     └────│ user_id (FK)     │    │     └──────────────┘
└──────────────┘          │ category_id (FK) │────┘
                          │ date             │
                          │ notes            │
                          └──────────────────┘

Relationships:
  • One USER     → Many EXPENSES  (@OneToMany, CascadeType.ALL)
  • One CATEGORY → Many EXPENSES  (@OneToMany, CascadeType.ALL)
  • One EXPENSE  → One USER       (@ManyToOne, FK: user_id)
  • One EXPENSE  → One CATEGORY   (@ManyToOne, FK: category_id)
```

Key design decisions:
- `amount` uses `BigDecimal` (not `double`) — exact decimal arithmetic, essential for monetary values.
- `date` uses `LocalDate` — date only, no time/timezone needed for expense tracking.
- `FetchType.LAZY` on all relationships — entities are not auto-joined on every query, preventing unnecessary DB load and the N+1 problem.
- `CascadeType.ALL` on User → Expenses means deleting a user automatically deletes all their expenses.

---

## ✅ Features Implemented

**User Management** — Full CRUD: create, read (all / by ID), update, delete. Email uniqueness enforced at service layer before hitting the DB. Password excluded from all API responses (DTO boundary).

**Category Management** — Full CRUD for expense categories. Name uniqueness enforced. Categories are the grouping mechanism for analytics.

**Expense Tracking** — Full CRUD with proper relational linking to User and Category. Client sends `userId` and `categoryId`; service resolves them to full entities and returns human-readable names (`userName`, `categoryName`) in the response.

**Pagination & Sorting** — The expense list endpoint accepts `page`, `size`, `sortBy`, and `direction` query parameters. Returns a Spring `Page<T>` object containing the items plus metadata (`totalElements`, `totalPages`, `isFirst`, `isLast`) — exactly what a frontend pagination control needs.

**Analytics: Spending by Category** — A custom JPQL query using `SUM()` and `GROUP BY` aggregates total spending per category for a given user — computed in the database, not in Java memory.

**Analytics: Monthly Summary** — Returns total spending and expense count for a specific year+month combination. Uses `COALESCE(SUM(...), 0)` so months with no expenses return zero instead of null.

**Input Validation** — All request DTOs are annotated with `@NotBlank`, `@NotNull`, `@Positive`, `@PastOrPresent`, `@Email`, `@Size`. Validation is triggered by `@Valid` in the controller. Failures return a structured 400 response with a field-by-field error map — no raw Spring error pages exposed.

**Centralised Exception Handling** — A single `@RestControllerAdvice` class handles all exception types: `ResourceNotFoundException` → 404, `DuplicateResourceException` → 409, validation failures → 400, anything unexpected → 500. Every error response follows the same JSON structure.

**Swagger / OpenAPI Documentation** — All endpoints are auto-documented and interactively testable at the Swagger UI URL below. No Postman or external tool needed.

---

## 🚀 Running Locally

**Prerequisites:** Java 21, Maven, PostgreSQL installed and running.

**Step 1 — Create the database.** Connect to PostgreSQL (via pgAdmin or psql) and run:

```sql
CREATE USER expenseuser WITH PASSWORD 'expensepass';
CREATE DATABASE expensetracker OWNER expenseuser;
GRANT ALL PRIVILEGES ON DATABASE expensetracker TO expenseuser;
```

**Step 2 — Run the app.** From IntelliJ, press the green Run button on `ExpenseTrackerApplication.java`. Hibernate will auto-create all tables on first startup (`ddl-auto=update`). Watch the console — Hibernate prints every SQL statement it runs so you can see the table creation happen.

**Step 3 — Open Swagger UI** in your browser:

```
http://localhost:8080/swagger-ui.html
```

---

## 📡 API Endpoints

### Base URL: `http://localhost:8080`
### Interactive Docs: `http://localhost:8080/swagger-ui.html`

---

### 👤 Users — `/api/users`

| Method | Endpoint | Description | Success Status |
|--------|----------|-------------|----------------|
| POST | `/api/users` | Create a new user | 201 Created |
| GET | `/api/users` | Get all users | 200 OK |
| GET | `/api/users/{id}` | Get user by ID | 200 OK |
| PUT | `/api/users/{id}` | Update user (full replacement) | 200 OK |
| DELETE | `/api/users/{id}` | Delete user (cascades to expenses) | 204 No Content |

**Sample request body (POST / PUT):**
```json
{
  "name": "Arjun Sharma",
  "email": "arjun@example.com",
  "password": "secret123"
}
```

**Sample response:**
```json
{
  "id": 1,
  "name": "Arjun Sharma",
  "email": "arjun@example.com"
}
```
> Password is never included in any response — intentional DTO boundary.

---

### 🏷️ Categories — `/api/categories`

| Method | Endpoint | Description | Success Status |
|--------|----------|-------------|----------------|
| POST | `/api/categories` | Create a category | 201 Created |
| GET | `/api/categories` | Get all categories | 200 OK |
| GET | `/api/categories/{id}` | Get category by ID | 200 OK |
| PUT | `/api/categories/{id}` | Update category | 200 OK |
| DELETE | `/api/categories/{id}` | Delete category | 204 No Content |

**Sample request body:**
```json
{
  "name": "Food",
  "description": "Dining, groceries, and snacks"
}
```

---

### 💸 Expenses — `/api/expenses`

| Method | Endpoint | Description | Success Status |
|--------|----------|-------------|----------------|
| POST | `/api/expenses` | Create an expense | 201 Created |
| GET | `/api/expenses?userId=1&page=0&size=10 | Get paginated expenses for a user | 200 OK |
| GET | `/api/expenses/{id}` | Get single expense by ID | 200 OK |
| PUT | `/api/expenses/{id}` | Update expense (full replacement) | 200 OK |
| DELETE | `/api/expenses/{id}` | Delete expense | 204 No Content |

**Pagination query parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `userId` | required | Filter by user |
| `page` | 0 | Page number (0-indexed) |
| `size` | 10 | Items per page |
| `sortBy` | `date` | Field to sort by |
| `direction` | `desc` | `asc` or `desc` |

**Sample request body:**
```json
{
  "description": "Lunch at office canteen",
  "amount": 150.00,
  "date": "2025-03-10",
  "notes": "Had thali",
  "userId": 1,
  "categoryId": 1
}
```

**Sample response** (IDs resolved to readable names):
```json
{
  "id": 1,
  "description": "Lunch at office canteen",
  "amount": 150.00,
  "date": "2025-03-10",
  "notes": "Had thali",
  "userId": 1,
  "userName": "Arjun Sharma",
  "categoryId": 1,
  "categoryName": "Food"
}
```

**Paginated list response shape:**
```json
{
  "content": [ ...expenses... ],
  "totalElements": 4,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

---

### 📊 Analytics — `/api/expenses/summary`

| Method | Endpoint | Description | Success Status |
|--------|----------|-------------|----------------|
| GET | `/api/expenses/summary/by-category?userId=1` | Total spending per category | 200 OK |
| GET | `/api/expenses/summary/monthly?userId=1&year=2025&month=3` | Monthly total + expense count | 200 OK |

**Spending by category response:**
```json
[
  { "categoryName": "Food",          "totalAmount": 1350.00 },
  { "categoryName": "Transport",     "totalAmount": 450.50  },
  { "categoryName": "Entertainment", "totalAmount": 649.00  }
]
```
> Computed via SQL `SUM() GROUP BY` in the database — not in Java memory.

**Monthly summary response:**
```json
{
  "year": 2025,
  "month": 3,
  "totalSpending": 2449.50,
  "expenseCount": 4
}
```

---

## ⚠️ Error Handling

All errors follow a consistent JSON structure. There are no raw Spring error pages exposed.

```json
{
  "timestamp": "2025-03-20T14:32:10",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found with id: 99"
}
```

| Scenario | HTTP Status | Exception |
|----------|-------------|-----------|
| ID not found in DB | 404 Not Found | `ResourceNotFoundException` |
| Duplicate email or category name | 409 Conflict | `DuplicateResourceException` |
| Invalid request body (@Valid fails) | 400 Bad Request | `MethodArgumentNotValidException` |
| Unexpected server error | 500 Internal Server Error | `Exception` (catch-all) |

Validation failures return a field-level error map:
```json
{
  "timestamp": "2025-03-20T14:32:10",
  "status": 400,
  "error": "Validation Failed",
  "fieldErrors": {
    "amount": "Amount must be a positive value",
    "email": "Please provide a valid email address"
  }
}
```

---

## 🧪 Manual Testing Sequence (Swagger UI)

Follow this order because `Expense` depends on both `User` and `Category` existing first.

**Phase 1** — `POST /api/users` → Create a user, note the returned `id`.

**Phase 2** — `POST /api/users` (same email again) → Verify 409 Conflict response.

**Phase 3** — `POST /api/categories` → Create Food, Transport, Entertainment. Note each `id`.

**Phase 4** — `GET /api/categories` → Verify all three appear.

**Phase 5** — `GET /api/categories/99` → Verify 404 with custom error message.

**Phase 6** — `POST /api/expenses` → Create 3–4 expenses with different categories and dates.

**Phase 7** — `GET /api/expenses?userId=1&page=0&size=2` → Observe paginated response. Try `page=1`.

**Phase 8** — `PUT /api/expenses/1` → Update an expense, verify changes reflected.

**Phase 9** — `GET /api/expenses/summary/by-category?userId=1` → See totals grouped by category.

**Phase 10** — `GET /api/expenses/summary/monthly?userId=1&year=2025&month=3` → See monthly total.

**Phase 11** — `DELETE /api/expenses/1` → 204 response. Then `GET /api/expenses/1` → 404.

---

## 🗺️ What's Coming Next

**JWT Authentication (Spring Security)** — Register/Login endpoints that return a JWT token. All existing endpoints will require the token in the `Authorization: Bearer <token>` header. Users will only be able to access their own expenses.

**JUnit + Mockito Tests** — Unit tests for the Service layer (mocking repositories), and integration tests for the Controller layer (using MockMvc).

**Docker** — A `docker-compose.yml` to containerise both the Spring Boot app and PostgreSQL so the entire stack runs with a single command on any machine.

**Kafka (Event Streaming)** — Publishing expense-created events to a Kafka topic, for future use cases like budget alerts or notification services.

---

## 🔑 Key Design Decisions Worth Knowing

**Why DTOs instead of returning Entities directly?** Entities represent database structure. DTOs represent API contract. Keeping them separate means you control exactly what data crosses your API boundary — for example, `password` is in the `User` entity but deliberately absent from `UserResponse`.

**Why `BigDecimal` for amount?** `double` and `float` use binary floating-point arithmetic which causes rounding errors (`0.1 + 0.2 = 0.30000000000000004`). For money, this is unacceptable. `BigDecimal` is exact decimal arithmetic.

**Why `FetchType.LAZY`?** Without it, loading any `Expense` would automatically JOIN and load the full `User` and `Category` objects every time — even when you don't need them. Lazy loading means related entities are only fetched when explicitly accessed.

**Why `@Transactional(readOnly = true)` on read methods?** It tells Hibernate to skip "dirty checking" (scanning all loaded entities for changes) since nothing will be written. This is a performance optimisation, especially under load.

**Why custom exceptions instead of throwing generic ones?** `ResourceNotFoundException` and `DuplicateResourceException` carry clear semantic meaning. The `GlobalExceptionHandler` maps each to the correct HTTP status code. Generic exceptions would all map to 500, which is both incorrect and unhelpful for clients.
