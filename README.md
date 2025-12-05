# LAB 07: Product Management System - Homework Exercises

## Student Information
- **Name:** Võ Trí Khôi
- **Student ID:** ITCSIU24045
- **Class:** Group 2

## Technologies Used
- Spring Boot 3.5.x
- Spring Data JPA
- Hibernate ORM
- MySQL 8.0
- Thymeleaf Template Engine
- Jakarta Validation
- Maven

## Setup Instructions
1. Import project into VS Code
2. Create database: `product_management`
3. Update `application.properties` with your MySQL credentials
4. Run: `mvn spring-boot:run`
5. Open browser: http://localhost:8080/products
6. Access dashboard: http://localhost:8080/dashboard

## Completed Features
- [x] Exercise 5: Advanced Search (12 points)
  - [x] Task 5.1: Multi-Criteria Search
  - [x] Task 5.2: Category Filter
  - [x] Task 5.3: Search with Pagination
- [x] Exercise 6: Validation (10 points)
  - [x] Task 6.1: Validation Annotations
  - [x] Task 6.2: Controller Validation
  - [x] Task 6.3: Display Validation Errors
- [x] Exercise 7: Sorting & Filtering (10 points)
  - [x] Task 7.1: Sorting
  - [x] Task 7.2: Category Filter
  - [x] Task 7.3: Combined Sorting and Filtering
- [x] Exercise 8: Statistics Dashboard (8 points)
  - [x] Task 8.1: Statistics Methods
  - [x] Task 8.2: Dashboard Controller
  - [x] Task 8.3: Dashboard View

## Project Structure
```
product-management/
├── src/
│   ├── main/
│   │   ├── java/com/example/product_management/
│   │   │   ├── ProductManagementApplication.java
│   │   │   ├── entity/
│   │   │   │   └── Product.java
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java
│   │   │   │   └── ProductServiceImpl.java
│   │   │   └── controller/
│   │   │       ├── ProductController.java
│   │   │       └── DashboardController.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── product-list.html
│   │           ├── product-form.html
│   │           └── dashboard.html
├── pom.xml
└── README.md
```

---

# Code Flow

## Exercise 5: Advanced Search (12 Points)

### Task 5.1: Multi-Criteria Search (6 Points)

**Objective:** Allow users to search products by multiple criteria (name, category, price range).

**Step A: User Fills Advanced Search Form**
1. **User Request**: User accesses `/products` and sees the Advanced Search form.
2. **View Layer** (`product-list.html`):
   - Form displays input fields for name, category dropdown, min price, and max price.
   - Form action points to `/products/advanced-search` with GET method.

**Step B: Controller Receives Search Parameters**
1. **Controller Layer** (`ProductController.java`):
   - The `advancedSearch` method mapped to `@GetMapping("/advanced-search")` is invoked.
   - All parameters are optional (`required = false`).
   - Calls `productService.advancedSearch(name, category, minPrice, maxPrice)`.

**Step C: Service Processes the Search**
1. **Service Layer** (`ProductServiceImpl.java`):
   - Converts empty strings to null for proper JPQL handling.
   - Delegates to repository method.

**Step D: Repository Executes Dynamic Query**
1. **Repository Layer** (`ProductRepository.java`):
   - Uses JPQL with conditional parameters.
   - NULL parameters are ignored in the WHERE clause.

**Step E: Results Rendered**
1. **View Layer**: Thymeleaf iterates over filtered products and displays them in the table.

---

### Task 5.2: Category Filter (3 Points)

**Objective:** Add a dropdown filter that shows all unique categories.

**Step A: Fetch Unique Categories**
1. **Repository Layer** (`ProductRepository.java`): Uses `SELECT DISTINCT` to get unique categories.

**Step B: Controller Adds Categories to Model**
1. **Controller Layer**: Every endpoint that returns to `product-list.html` includes categories.

**Step C: View Renders Dropdown with Auto-Submit**
1. **View Layer** (`product-list.html`):
   - `onchange="this.form.submit()"` triggers form submission when selection changes.
   - Hidden fields preserve current sort state.

---

### Task 5.3: Search with Pagination (3 Points)

**Objective:** Implement pagination for search results.

**Step A: User Searches with Keyword**
1. **User Request**: User enters keyword and submits search form to `/products/search`.

**Step B: Controller Creates Pageable Request**
1. **Controller Layer** (`ProductController.java`):
   - Creates `Pageable` object with page number and size.
   - Returns `Page<Product>` with pagination metadata.

**Step C: Repository Returns Paginated Results**
1. **Repository Layer** (`ProductRepository.java`):
   - Spring Data JPA auto-generates implementation.

**Step D: View Renders Pagination Controls**
1. **View Layer** (`product-list.html`): Displays Previous/Next buttons and page numbers.

---

## Exercise 6: Validation (10 Points)

### Task 6.1: Validation Annotations (5 Points)

**Objective:** Add validation constraints to the Product entity.

**Entity Layer** (`Product.java`):
- Uses Jakarta Validation annotations to enforce business rules.

---

### Task 6.2: Controller Validation (3 Points)

**Objective:** Validate input in the controller and handle errors.

**Step A: User Submits Form**
1. **User Request**: User fills product form and submits POST to `/products/save`.

**Step B: Controller Validates Input**
1. **Controller Layer** (`ProductController.java`):
   - `@Valid` triggers validation.
   - `BindingResult` captures errors (must immediately follow `@Valid` parameter).

---

### Task 6.3: Display Validation Errors (2 Points)

**Objective:** Show validation errors in the form.

**View Layer** (`product-form.html`):
- `th:errorclass="error"` adds CSS class when field has errors.
- `th:if="${#fields.hasErrors('fieldName')}"` conditionally shows error message.
- `th:errors="*{fieldName}"` displays the validation message.

---

## Exercise 7: Sorting & Filtering (10 Points)

### Task 7.1: Sorting (5 Points)

**Objective:** Allow users to sort products by clicking column headers.

**Step A: User Clicks Column Header**
1. **User Request**: User clicks "Name" column header.
2. **URL Generated**: `/products?sortBy=name&sortDir=asc`

**Step B: Controller Processes Sort Parameters**
1. **Controller Layer** (`ProductController.java`): Creates `Sort` object based on parameters.

**Step C: Service Delegates to Repository**
1. **Service Layer** (`ProductServiceImpl.java`): Calls `findAll(sort)`.

**Step D: View Renders Sortable Headers**
1. **View Layer** (`product-list.html`):
   - Clicking toggles between ASC and DESC.
   - Arrow indicator shows current sort direction.

---

### Task 7.2 & 7.3: Combined Sorting and Filtering (5 Points)

**Objective:** Combine category filtering with sorting.

**View Preserves Both States:**
- Category filter preserves sort parameters via hidden fields.
- Sort links preserve category parameter in URL.

---

## Exercise 8: Statistics Dashboard (8 Points)

### Task 8.1: Statistics Methods (4 Points)

**Objective:** Add repository methods for calculating statistics.

**Repository Layer** (`ProductRepository.java`):
- `countByCategory`: Count products by category
- `calculateTotalValue`: Sum of (price × quantity) for all products
- `calculateAveragePrice`: Average product price
- `findLowStockProducts`: Products with quantity below threshold
- `findTop5ByOrderByCreatedAtDesc`: 5 most recently added products

---

### Task 8.2: Dashboard Controller (2 Points)

**Objective:** Create controller to serve dashboard statistics.

**Step A: User Accesses Dashboard**
1. **User Request**: User navigates to `/dashboard`.

**Step B: Controller Gathers Statistics**
1. **Controller Layer** (`DashboardController.java`):
   - Fetches total products, products by category, total value, average price
   - Gets low stock alerts and recent products
   - Returns `dashboard` view

---

### Task 8.3: Dashboard View (2 Points)

**Objective:** Create visual dashboard with statistics cards, charts, and tables.

**View Layer** (`dashboard.html`):
- Statistics cards showing totals
- Bar chart for products by category
- Low stock alerts table
- Recent products table
