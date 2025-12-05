# REPORT LAB 07 HOMEWORK
## Product Management System - Spring Boot Application

This document explains the workflow and implementation details of all exercises (5-8) in the Product Management System.

---

## Table of Contents

1. [Exercise 5: Advanced Search](#exercise-5-advanced-search-12-points)
2. [Exercise 6: Validation](#exercise-6-validation-10-points)
3. [Exercise 7: Sorting & Filtering](#exercise-7-sorting--filtering-10-points)
4. [Exercise 8: Statistics Dashboard](#exercise-8-statistics-dashboard-8-points)

---

## Exercise 5: Advanced Search (12 Points)

### Task 5.1: Multi-Criteria Search (6 Points)

**Objective:** Allow users to search products by multiple criteria simultaneously.

#### Repository Layer (`ProductRepository.java`)

```java
@Query("SELECT p FROM Product p WHERE " +
       "(:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
       "(:category IS NULL OR :category = '' OR p.category = :category) AND " +
       "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
       "(:maxPrice IS NULL OR p.price <= :maxPrice)")
List<Product> searchProducts(@Param("name") String name,
                            @Param("category") String category,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice);
```

**How it works:**
- Uses JPQL (Java Persistence Query Language) with conditional parameters
- Each condition checks if the parameter is NULL first - if NULL, that condition is ignored
- `LIKE LOWER(CONCAT('%', :name, '%'))` performs case-insensitive partial matching
- All conditions are combined with `AND` operator

#### Service Layer (`ProductServiceImpl.java`)

```java
@Override
public List<Product> advancedSearch(String name, String category, 
                                    BigDecimal minPrice, BigDecimal maxPrice) {
    // Convert empty strings to null for proper query handling
    String searchName = (name != null && name.trim().isEmpty()) ? null : name;
    String searchCategory = (category != null && category.trim().isEmpty()) ? null : category;
    return productRepository.searchProducts(searchName, searchCategory, minPrice, maxPrice);
}
```

**How it works:**
- Converts empty strings to null to ensure the JPQL query works correctly
- Delegates to repository method

#### Controller Layer (`ProductController.java`)

```java
@GetMapping("/advanced-search")
public String advancedSearch(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        Model model) {
    
    List<Product> products = productService.advancedSearch(name, category, minPrice, maxPrice);
    
    model.addAttribute("products", products);
    model.addAttribute("searchName", name);
    model.addAttribute("searchCategory", category);
    model.addAttribute("searchMinPrice", minPrice);
    model.addAttribute("searchMaxPrice", maxPrice);
    model.addAttribute("categories", productService.getAllCategories());
    
    return "product-list";
}
```

**How it works:**
- `@RequestParam(required = false)` makes all parameters optional
- Calls service method with search criteria
- Adds search results and form values to the model for view rendering

#### View Layer (`product-list.html`)

```html
<div class="advanced-search-form">
    <h3>🔎 Advanced Search</h3>
    <form th:action="@{/products/advanced-search}" method="get">
        <div class="form-row">
            <div class="form-group">
                <label for="searchName">Product Name</label>
                <input type="text" id="searchName" name="name" 
                       th:value="${searchName}" placeholder="Search by name..." />
            </div>
            <div class="form-group">
                <label for="searchCategory">Category</label>
                <select id="searchCategory" name="category">
                    <option value="">All Categories</option>
                    <option th:each="cat : ${categories}" 
                            th:value="${cat}" 
                            th:text="${cat}"
                            th:selected="${cat == searchCategory}">
                    </option>
                </select>
            </div>
            <div class="form-group">
                <label for="minPrice">Min Price ($)</label>
                <input type="number" id="minPrice" name="minPrice" 
                       step="0.01" min="0" th:value="${searchMinPrice}" />
            </div>
            <div class="form-group">
                <label for="maxPrice">Max Price ($)</label>
                <input type="number" id="maxPrice" name="maxPrice" 
                       step="0.01" min="0" th:value="${searchMaxPrice}" />
            </div>
        </div>
        <button type="submit" class="btn btn-primary">🔍 Search</button>
        <a th:href="@{/products}" class="btn btn-danger">❌ Clear</a>
    </form>
</div>
```

**Workflow Diagram:**

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│   User fills    │────▶│  Form submits    │────▶│   Controller       │
│   search form   │     │  GET request     │     │   receives params  │
└─────────────────┘     └──────────────────┘     └────────────────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│   View renders  │◀────│  Model contains  │◀────│   Service calls    │
│   results       │     │  search results  │     │   Repository       │
└─────────────────┘     └──────────────────┘     └────────────────────┘
```

---

### Task 5.2: Category Filter (3 Points)

**Objective:** Add a dropdown that shows all unique categories for filtering.

#### Repository Layer

```java
@Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
List<String> findAllCategories();
```

**How it works:**
- `SELECT DISTINCT` returns only unique category values
- `ORDER BY` sorts categories alphabetically

#### View Layer

```html
<div class="filter-section">
    <form th:action="@{/products}" method="get">
        <label><strong>Filter by Category:</strong></label>
        <select name="category" onchange="this.form.submit()">
            <option value="">All Categories</option>
            <option th:each="cat : ${categories}" 
                    th:value="${cat}" 
                    th:text="${cat}"
                    th:selected="${cat == selectedCategory}">
            </option>
        </select>
        <!-- Preserve sorting when filtering -->
        <input type="hidden" name="sortBy" th:value="${sortBy}" />
        <input type="hidden" name="sortDir" th:value="${sortDir}" />
    </form>
</div>
```

**How it works:**
- `onchange="this.form.submit()"` auto-submits when selection changes
- Hidden inputs preserve current sorting state
- `th:selected` maintains the selected value after form submission

---

### Task 5.3: Search with Pagination (3 Points)

**Objective:** Implement pagination for search results.

#### Repository Layer

```java
Page<Product> findByNameContaining(String keyword, Pageable pageable);
```

**How it works:**
- Spring Data JPA automatically generates implementation
- Returns `Page<Product>` containing results and pagination metadata

#### Controller Layer

```java
@GetMapping("/search")
public String searchProducts(
        @RequestParam("keyword") String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage = productService.searchProducts(keyword, pageable);
    
    model.addAttribute("products", productPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", productPage.getTotalPages());
    model.addAttribute("totalItems", productPage.getTotalElements());
    
    return "product-list";
}
```

**How it works:**
- `PageRequest.of(page, size)` creates a Pageable object
- `Page` interface provides: content, total pages, total elements, etc.

#### View Layer - Pagination Controls

```html
<div th:if="${totalPages != null and totalPages > 1}" class="pagination">
    <!-- Previous Button -->
    <a th:if="${currentPage > 0}" 
       th:href="@{/products/search(keyword=${keyword},page=${currentPage - 1},size=10)}">
       « Previous
    </a>
    
    <!-- Page Numbers -->
    <th:block th:each="i : ${#numbers.sequence(0, totalPages - 1)}">
        <a th:if="${i != currentPage}"
           th:href="@{/products/search(keyword=${keyword},page=${i},size=10)}"
           th:text="${i + 1}">1</a>
        <span th:if="${i == currentPage}" class="active" th:text="${i + 1}">1</span>
    </th:block>
    
    <!-- Next Button -->
    <a th:if="${currentPage < totalPages - 1}" 
       th:href="@{/products/search(keyword=${keyword},page=${currentPage + 1},size=10)}">
       Next »
    </a>
</div>
```

**Pagination Workflow:**

```
Page 0: Products 1-10   ──▶  Page 1: Products 11-20  ──▶  Page 2: Products 21-30
    │                            │                            │
    ▼                            ▼                            ▼
[1] 2  3  Next              Prev [2] 3  Next            Prev 1  2  [3]
```

---

## Exercise 6: Validation (10 Points)

### Task 6.1: Validation Annotations (5 Points)

**Objective:** Add validation constraints to the Product entity.

#### Entity Layer (`Product.java`)

```java
import jakarta.validation.constraints.*;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product code is required")
    @Size(min = 3, max = 20, message = "Product code must be 3-20 characters")
    @Pattern(regexp = "^P\\d{3,}$", message = "Product code must start with P followed by at least 3 numbers")
    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price is too high")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity;
    
    @NotBlank(message = "Category is required")
    @Column(length = 50)
    private String category;
}
```

**Validation Annotations Explained:**

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotBlank` | String must not be null, empty, or whitespace | Required fields |
| `@NotNull` | Value must not be null | Non-string required fields |
| `@Size` | String length constraints | `@Size(min=3, max=20)` |
| `@Pattern` | Must match regex pattern | `@Pattern(regexp="^P\\d{3,}$")` |
| `@DecimalMin` | Minimum decimal value | `@DecimalMin("0.01")` |
| `@DecimalMax` | Maximum decimal value | `@DecimalMax("999999.99")` |
| `@Min` | Minimum integer value | `@Min(0)` |

---

### Task 6.2: Controller Validation (3 Points)

**Objective:** Validate input in the controller and handle errors.

```java
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@PostMapping("/save")
public String saveProduct(
        @Valid @ModelAttribute("product") Product product,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {
    
    // Check for validation errors
    if (result.hasErrors()) {
        return "product-form";  // Return to form with errors
    }
    
    try {
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }
    
    return "redirect:/products";
}
```

**How it works:**
1. `@Valid` triggers validation of the Product object
2. `BindingResult` captures validation errors (must be immediately after `@Valid` parameter)
3. If errors exist, return to form without saving
4. If valid, save and redirect with success message

**Validation Flow:**

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│   User submits  │────▶│   @Valid checks  │────▶│   Errors found?    │
│   form          │     │   Product fields │     │                    │
└─────────────────┘     └──────────────────┘     └────────────────────┘
                                                          │
                                    ┌─────────────────────┴─────────────────────┐
                                    │                                           │
                                    ▼                                           ▼
                        ┌──────────────────┐                       ┌──────────────────┐
                        │   YES: Return    │                       │   NO: Save to    │
                        │   to form with   │                       │   database and   │
                        │   error messages │                       │   redirect       │
                        └──────────────────┘                       └──────────────────┘
```

---

### Task 6.3: Display Validation Errors (2 Points)

**Objective:** Show validation errors in the form.

#### View Layer (`product-form.html`)

```html
<div class="form-group">
    <label for="productCode">Product Code <span class="required-indicator">*</span></label>
    <input type="text" 
           id="productCode" 
           th:field="*{productCode}" 
           th:errorclass="error"
           placeholder="Enter product code (e.g., P001)" />
    <span th:if="${#fields.hasErrors('productCode')}" 
          th:errors="*{productCode}" 
          class="error-message">Error</span>
    <span class="validation-hint">Must start with P followed by at least 3 numbers</span>
</div>
```

#### CSS Styles

```css
.error { 
    border-color: red !important; 
}

.error-message { 
    color: red; 
    font-size: 12px; 
    margin-top: 5px; 
    display: block; 
}

.validation-hint {
    color: #666;
    font-size: 11px;
    margin-top: 3px;
}
```

**How it works:**
- `th:errorclass="error"` adds "error" class when field has errors
- `th:if="${#fields.hasErrors('fieldName')}"` checks if field has errors
- `th:errors="*{fieldName}"` displays the error message

---

## Exercise 7: Sorting & Filtering (10 Points)

### Task 7.1: Sorting (5 Points)

**Objective:** Allow users to sort products by clicking column headers.

#### Controller Layer

```java
@GetMapping
public String listProducts(
        @RequestParam(required = false) String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(required = false) String category,
        Model model) {
    
    List<Product> products;
    
    if (sortBy != null && !sortBy.isEmpty()) {
        Sort sort = sortDir.equals("asc") ? 
            Sort.by(sortBy).ascending() : 
            Sort.by(sortBy).descending();
        products = productService.getAllProducts(sort);
    } else {
        products = productService.getAllProducts();
    }
    
    model.addAttribute("products", products);
    model.addAttribute("sortBy", sortBy);
    model.addAttribute("sortDir", sortDir);
    
    return "product-list";
}
```

#### Service Layer

```java
@Override
public List<Product> getAllProducts(Sort sort) {
    return productRepository.findAll(sort);
}
```

#### View Layer - Sortable Headers

```html
<th>
    <a th:href="@{/products(sortBy='name',sortDir=${sortDir=='asc' and sortBy=='name' ? 'desc' : 'asc'},category=${selectedCategory})}">
        Name
        <span class="sort-indicator" th:if="${sortBy=='name'}" 
              th:text="${sortDir=='asc' ? '↑' : '↓'}"></span>
    </a>
</th>
```

**How it works:**
- Clicking header toggles between ascending and descending
- Current sort column shows arrow indicator (↑ or ↓)
- Category filter is preserved in URL

**Sorting Logic:**

```
Click "Name" (unsorted)  ──▶  Sort by name ASC (↑)
Click "Name" (ASC)       ──▶  Sort by name DESC (↓)
Click "Name" (DESC)      ──▶  Sort by name ASC (↑)
```

---

### Task 7.2 & 7.3: Category Filter with Sorting (5 Points)

**Objective:** Combine filtering and sorting in one interface.

```java
// In Controller
if (category != null && !category.isEmpty()) {
    products = productService.getProductsByCategory(category);
    if (sortBy != null && !sortBy.isEmpty()) {
        Sort sort = sortDir.equals("asc") ? 
            Sort.by(sortBy).ascending() : 
            Sort.by(sortBy).descending();
        products = productService.getAllProducts(sort).stream()
            .filter(p -> p.getCategory().equals(category))
            .toList();
    }
}
```

**Combined Workflow:**

```
┌─────────────────────────────────────────────────────────────┐
│                    Product List View                        │
├─────────────────────────────────────────────────────────────┤
│  Filter: [Electronics ▼]     Sort: Name ↑                   │
├─────────────────────────────────────────────────────────────┤
│  Name ↑    │  Price  │  Quantity  │  Category              │
├────────────┼─────────┼────────────┼────────────────────────┤
│  iPhone    │  $999   │  50        │  Electronics           │
│  Laptop    │  $1299  │  30        │  Electronics           │
│  Tablet    │  $599   │  75        │  Electronics           │
└─────────────────────────────────────────────────────────────┘
```

---

## Exercise 8: Statistics Dashboard (8 Points)

### Task 8.1: Statistics Methods (4 Points)

**Objective:** Add repository methods for calculating statistics.

#### Repository Layer

```java
// Count products by category
@Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
long countByCategory(@Param("category") String category);

// Calculate total inventory value (price × quantity for all products)
@Query("SELECT SUM(p.price * p.quantity) FROM Product p")
BigDecimal calculateTotalValue();

// Calculate average product price
@Query("SELECT AVG(p.price) FROM Product p")
BigDecimal calculateAveragePrice();

// Find products with low stock
@Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
List<Product> findLowStockProducts(@Param("threshold") int threshold);

// Get recent products (last 5 added)
List<Product> findTop5ByOrderByCreatedAtDesc();
```

---

### Task 8.2: Dashboard Controller (2 Points)

```java
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String showDashboard(Model model) {
        // Total products count
        long totalProducts = productService.getTotalProductCount();
        model.addAttribute("totalProducts", totalProducts);
        
        // Products by category
        List<String> categories = productService.getAllCategories();
        Map<String, Long> productsByCategory = new HashMap<>();
        for (String category : categories) {
            productsByCategory.put(category, productService.getProductCountByCategory(category));
        }
        model.addAttribute("categories", categories);
        model.addAttribute("productsByCategory", productsByCategory);
        
        // Total inventory value
        BigDecimal totalValue = productService.getTotalInventoryValue();
        model.addAttribute("totalValue", totalValue);
        
        // Average product price
        BigDecimal averagePrice = productService.getAveragePrice();
        model.addAttribute("averagePrice", averagePrice);
        
        // Low stock alerts (quantity < 10)
        List<Product> lowStockProducts = productService.getLowStockProducts(10);
        model.addAttribute("lowStockProducts", lowStockProducts);
        
        // Recent products (last 5 added)
        List<Product> recentProducts = productService.getRecentProducts();
        model.addAttribute("recentProducts", recentProducts);
        
        return "dashboard";
    }
}
```

---

### Task 8.3: Dashboard View (2 Points)

#### Statistics Cards

```html
<div class="stats-grid">
    <div class="stat-card">
        <div class="stat-value" th:text="${totalProducts}">0</div>
        <div class="stat-label">📦 Total Products</div>
    </div>
    
    <div class="stat-card success">
        <div class="stat-value" th:text="'$' + ${#numbers.formatDecimal(totalValue, 1, 2)}">$0.00</div>
        <div class="stat-label">💰 Total Inventory Value</div>
    </div>
    
    <div class="stat-card info">
        <div class="stat-value" th:text="'$' + ${#numbers.formatDecimal(averagePrice, 1, 2)}">$0.00</div>
        <div class="stat-label">📈 Average Price</div>
    </div>
    
    <div class="stat-card warning">
        <div class="stat-value" th:text="${lowStockCount}">0</div>
        <div class="stat-label">⚠️ Low Stock Items</div>
    </div>
</div>
```

#### Bar Chart for Categories

```html
<div class="bar-chart">
    <div class="bar-item" th:each="category : ${categories}">
        <span class="bar-label" th:text="${category}">Category</span>
        <div class="bar-container">
            <div class="bar-fill" 
                 th:style="'width: ' + ${totalProducts > 0 ? (productsByCategory[category] * 100 / totalProducts) : 0} + '%'">
            </div>
        </div>
        <span class="bar-value" th:text="${productsByCategory[category]}">0</span>
    </div>
</div>
```

#### Low Stock Alerts Table

```html
<table>
    <thead>
        <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Category</th>
            <th>Quantity</th>
            <th>Price</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="product : ${lowStockProducts}" class="low-stock-row">
            <td th:text="${product.productCode}">P001</td>
            <td th:text="${product.name}">Product Name</td>
            <td th:text="${product.category}">Category</td>
            <td th:text="${product.quantity}">5</td>
            <td th:text="'$' + ${#numbers.formatDecimal(product.price, 1, 2)}">$99.99</td>
        </tr>
    </tbody>
</table>
```

**Dashboard Layout:**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     📊 Product Statistics Dashboard                      │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐            │
│  │    150    │  │ $45,230   │  │  $301.53  │  │     8     │            │
│  │  Products │  │   Value   │  │ Avg Price │  │ Low Stock │            │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘            │
├─────────────────────────────────────────────────────────────────────────┤
│  📂 Products by Category                                                │
│  ┌──────────────────────────────────────────────────────────┐          │
│  │ Electronics  ████████████████████  45                    │          │
│  │ Furniture    ██████████████  35                          │          │
│  │ Clothing     ████████████  30                            │          │
│  │ Books        ████████  20                                │          │
│  │ Food         ████████  20                                │          │
│  └──────────────────────────────────────────────────────────┘          │
├─────────────────────────────────────────────────────────────────────────┤
│  ⚠️ Low Stock Alerts (Quantity < 10)                                    │
│  ┌──────┬────────────────┬─────────────┬──────────┬─────────┐          │
│  │ Code │ Name           │ Category    │ Quantity │ Price   │          │
│  ├──────┼────────────────┼─────────────┼──────────┼─────────┤          │
│  │ P001 │ Widget A       │ Electronics │    3     │ $29.99  │          │
│  │ P015 │ Gadget B       │ Electronics │    5     │ $49.99  │          │
│  └──────┴────────────────┴─────────────┴──────────┴─────────┘          │
├─────────────────────────────────────────────────────────────────────────┤
│  🕐 Recent Products (Last 5 Added)                                      │
│  ┌──────┬────────────────┬─────────────┬─────────┬─────────────────┐   │
│  │ Code │ Name           │ Category    │ Price   │ Added           │   │
│  ├──────┼────────────────┼─────────────┼─────────┼─────────────────┤   │
│  │ P150 │ New Product    │ Clothing    │ $59.99  │ Dec 05, 2025    │   │
│  └──────┴────────────────┴─────────────┴─────────┴─────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure Summary

```
src/main/java/com/example/product_management/
├── ProductManagementApplication.java    # Main entry point
├── controller/
│   ├── ProductController.java           # Product CRUD + Search + Sort
│   └── DashboardController.java         # Statistics dashboard
├── entity/
│   └── Product.java                     # Entity with validations
├── repository/
│   └── ProductRepository.java           # Data access layer
└── service/
    ├── ProductService.java              # Service interface
    └── ProductServiceImpl.java          # Service implementation

src/main/resources/templates/
├── product-list.html                    # Product listing with search/sort/filter
├── product-form.html                    # Add/Edit form with validation
└── dashboard.html                       # Statistics dashboard
```

---

## URLs Reference

| URL | Method | Description |
|-----|--------|-------------|
| `/products` | GET | List all products (with sort/filter) |
| `/products/new` | GET | Show add product form |
| `/products/edit/{id}` | GET | Show edit product form |
| `/products/save` | POST | Save product (with validation) |
| `/products/delete/{id}` | GET | Delete product |
| `/products/search` | GET | Search with pagination |
| `/products/advanced-search` | GET | Multi-criteria search |
| `/dashboard` | GET | Statistics dashboard |

---

## Technologies Used

- **Spring Boot 3.5.x** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM
- **Thymeleaf** - Template engine
- **Jakarta Validation** - Bean validation
- **MySQL** - Database
- **Maven** - Build tool

---

## How to Run

1. Ensure MySQL is running and database is configured in `application.properties`
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Access the application at: `http://localhost:8080/products`
4. Access the dashboard at: `http://localhost:8080/dashboard`
