# LAB 07: Product Management System - Homework Exercises

## Student Information
- **Name:** [Your Name]
- **Student ID:** [Your Student ID]
- **Class:** [Your Class]

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

```html
<form th:action="@{/products/advanced-search}" method="get">
    <input type="text" name="name" placeholder="Search by name..." />
    <select name="category">
        <option value="">All Categories</option>
        <option th:each="cat : ${categories}" th:value="${cat}" th:text="${cat}"></option>
    </select>
    <input type="number" name="minPrice" step="0.01" placeholder="Min Price" />
    <input type="number" name="maxPrice" step="0.01" placeholder="Max Price" />
    <button type="submit">Search</button>
</form>
```

**Step B: Controller Receives Search Parameters**
1. **Controller Layer** (`ProductController.java`):
   - The `advancedSearch` method mapped to `@GetMapping("/advanced-search")` is invoked.
   - All parameters are optional (`required = false`).
   - Calls `productService.advancedSearch(name, category, minPrice, maxPrice)`.

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
    model.addAttribute("categories", productService.getAllCategories());
    return "product-list";
}
```

**Step C: Service Processes the Search**
1. **Service Layer** (`ProductServiceImpl.java`):
   - Converts empty strings to null for proper JPQL handling.
   - Delegates to repository method.

```java
@Override
public List<Product> advancedSearch(String name, String category, 
                                    BigDecimal minPrice, BigDecimal maxPrice) {
    String searchName = (name != null && name.trim().isEmpty()) ? null : name;
    String searchCategory = (category != null && category.trim().isEmpty()) ? null : category;
    return productRepository.searchProducts(searchName, searchCategory, minPrice, maxPrice);
}
```

**Step D: Repository Executes Dynamic Query**
1. **Repository Layer** (`ProductRepository.java`):
   - Uses JPQL with conditional parameters.
   - NULL parameters are ignored in the WHERE clause.

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

**Step E: Results Rendered**
1. **View Layer**: Thymeleaf iterates over filtered products and displays them in the table.

---

### Task 5.2: Category Filter (3 Points)

**Objective:** Add a dropdown filter that shows all unique categories.

**Step A: Fetch Unique Categories**
1. **Repository Layer** (`ProductRepository.java`):

```java
@Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
List<String> findAllCategories();
```

**Step B: Controller Adds Categories to Model**
1. **Controller Layer**: Every endpoint that returns to `product-list.html` includes categories:

```java
model.addAttribute("categories", productService.getAllCategories());
```

**Step C: View Renders Dropdown with Auto-Submit**
1. **View Layer** (`product-list.html`):
   - `onchange="this.form.submit()"` triggers form submission when selection changes.
   - Hidden fields preserve current sort state.

```html
<form th:action="@{/products}" method="get">
    <select name="category" onchange="this.form.submit()">
        <option value="">All Categories</option>
        <option th:each="cat : ${categories}" 
                th:value="${cat}" 
                th:text="${cat}"
                th:selected="${cat == selectedCategory}">
        </option>
    </select>
    <input type="hidden" name="sortBy" th:value="${sortBy}" />
    <input type="hidden" name="sortDir" th:value="${sortDir}" />
</form>
```

---

### Task 5.3: Search with Pagination (3 Points)

**Objective:** Implement pagination for search results.

**Step A: User Searches with Keyword**
1. **User Request**: User enters keyword and submits search form to `/products/search`.

**Step B: Controller Creates Pageable Request**
1. **Controller Layer** (`ProductController.java`):
   - Creates `Pageable` object with page number and size.
   - Returns `Page<Product>` with pagination metadata.

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

**Step C: Repository Returns Paginated Results**
1. **Repository Layer** (`ProductRepository.java`):
   - Spring Data JPA auto-generates implementation.

```java
Page<Product> findByNameContaining(String keyword, Pageable pageable);
```

**Step D: View Renders Pagination Controls**
1. **View Layer** (`product-list.html`):

```html
<div th:if="${totalPages != null and totalPages > 1}" class="pagination">
    <a th:if="${currentPage > 0}" 
       th:href="@{/products/search(keyword=${keyword},page=${currentPage - 1})}">
       « Previous
    </a>
    
    <th:block th:each="i : ${#numbers.sequence(0, totalPages - 1)}">
        <span th:if="${i == currentPage}" class="active" th:text="${i + 1}">1</span>
        <a th:if="${i != currentPage}"
           th:href="@{/products/search(keyword=${keyword},page=${i})}"
           th:text="${i + 1}">1</a>
    </th:block>
    
    <a th:if="${currentPage < totalPages - 1}" 
       th:href="@{/products/search(keyword=${keyword},page=${currentPage + 1})}">
       Next »
    </a>
</div>
```

---

## Exercise 6: Validation (10 Points)

### Task 6.1: Validation Annotations (5 Points)

**Objective:** Add validation constraints to the Product entity.

**Entity Layer** (`Product.java`):
- Uses Jakarta Validation annotations to enforce business rules.

```java
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product code is required")
    @Size(min = 3, max = 20, message = "Product code must be 3-20 characters")
    @Pattern(regexp = "^P\\d{3,}$", message = "Product code must start with P followed by at least 3 numbers")
    private String productCode;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price is too high")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @NotBlank(message = "Category is required")
    private String category;
}
```

**Validation Annotations Summary:**

| Annotation | Field | Rule |
|------------|-------|------|
| `@NotBlank` | productCode, name, category | Cannot be null, empty, or whitespace |
| `@Size` | productCode, name | Length constraints |
| `@Pattern` | productCode | Must match `P` + 3+ digits (e.g., P001) |
| `@NotNull` | price, quantity | Cannot be null |
| `@DecimalMin` | price | Minimum value 0.01 |
| `@DecimalMax` | price | Maximum value 999999.99 |
| `@Min` | quantity | Minimum value 0 |

---

### Task 6.2: Controller Validation (3 Points)

**Objective:** Validate input in the controller and handle errors.

**Step A: User Submits Form**
1. **User Request**: User fills product form and submits POST to `/products/save`.

**Step B: Controller Validates Input**
1. **Controller Layer** (`ProductController.java`):
   - `@Valid` triggers validation.
   - `BindingResult` captures errors (must immediately follow `@Valid` parameter).

```java
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

**Validation Flow:**
```
User submits form → @Valid triggers validation → Errors found?
                                                      │
                    ┌─────────────────────────────────┴─────────────────────────────────┐
                    │ YES                                                               │ NO
                    ▼                                                                   ▼
            Return to form with                                                 Save to database
            error messages displayed                                            and redirect
```

---

### Task 6.3: Display Validation Errors (2 Points)

**Objective:** Show validation errors in the form.

**View Layer** (`product-form.html`):

```html
<div class="form-group">
    <label for="productCode">Product Code *</label>
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

**CSS Styles:**
```css
.error { 
    border-color: red !important; 
}

.error-message { 
    color: red; 
    font-size: 12px; 
}
```

**How it works:**
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
1. **Controller Layer** (`ProductController.java`):

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

**Step C: Service Delegates to Repository**
1. **Service Layer** (`ProductServiceImpl.java`):

```java
@Override
public List<Product> getAllProducts(Sort sort) {
    return productRepository.findAll(sort);
}
```

**Step D: View Renders Sortable Headers**
1. **View Layer** (`product-list.html`):
   - Clicking toggles between ASC and DESC.
   - Arrow indicator shows current sort direction.

```html
<th>
    <a th:href="@{/products(sortBy='name',sortDir=${sortDir=='asc' and sortBy=='name' ? 'desc' : 'asc'})}">
        Name
        <span th:if="${sortBy=='name'}" th:text="${sortDir=='asc' ? '↑' : '↓'}"></span>
    </a>
</th>
```

**Sorting Logic:**
```
Click "Name" (unsorted)  →  Sort by name ASC (↑)
Click "Name" (ASC ↑)     →  Sort by name DESC (↓)
Click "Name" (DESC ↓)    →  Sort by name ASC (↑)
```

---

### Task 7.2 & 7.3: Combined Sorting and Filtering (5 Points)

**Objective:** Combine category filtering with sorting.

**Controller Logic:**
```java
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

**View Preserves Both States:**
```html
<!-- Category filter preserves sort -->
<input type="hidden" name="sortBy" th:value="${sortBy}" />
<input type="hidden" name="sortDir" th:value="${sortDir}" />

<!-- Sort links preserve category -->
<a th:href="@{/products(sortBy='name',sortDir=...,category=${selectedCategory})}">
```

---

## Exercise 8: Statistics Dashboard (8 Points)

### Task 8.1: Statistics Methods (4 Points)

**Objective:** Add repository methods for calculating statistics.

**Repository Layer** (`ProductRepository.java`):

```java
// Count products by category
@Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
long countByCategory(@Param("category") String category);

// Calculate total inventory value (price × quantity)
@Query("SELECT SUM(p.price * p.quantity) FROM Product p")
BigDecimal calculateTotalValue();

// Calculate average product price
@Query("SELECT AVG(p.price) FROM Product p")
BigDecimal calculateAveragePrice();

// Find products with low stock (quantity < threshold)
@Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
List<Product> findLowStockProducts(@Param("threshold") int threshold);

// Get 5 most recently added products
List<Product> findTop5ByOrderByCreatedAtDesc();
```

---

### Task 8.2: Dashboard Controller (2 Points)

**Objective:** Create controller to serve dashboard statistics.

**Step A: User Accesses Dashboard**
1. **User Request**: User navigates to `/dashboard`.

**Step B: Controller Gathers Statistics**
1. **Controller Layer** (`DashboardController.java`):

```java
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String showDashboard(Model model) {
        // Total products
        model.addAttribute("totalProducts", productService.getTotalProductCount());
        
        // Products by category
        List<String> categories = productService.getAllCategories();
        Map<String, Long> productsByCategory = new HashMap<>();
        for (String category : categories) {
            productsByCategory.put(category, productService.getProductCountByCategory(category));
        }
        model.addAttribute("productsByCategory", productsByCategory);
        
        // Financial statistics
        model.addAttribute("totalValue", productService.getTotalInventoryValue());
        model.addAttribute("averagePrice", productService.getAveragePrice());
        
        // Alerts
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));
        
        // Recent activity
        model.addAttribute("recentProducts", productService.getRecentProducts());
        
        return "dashboard";
    }
}
```

---

### Task 8.3: Dashboard View (2 Points)

**Objective:** Create visual dashboard with statistics cards, charts, and tables.

**View Layer** (`dashboard.html`):

**Statistics Cards:**
```html
<div class="stats-grid">
    <div class="stat-card">
        <div class="stat-value" th:text="${totalProducts}">0</div>
        <div class="stat-label">📦 Total Products</div>
    </div>
    
    <div class="stat-card">
        <div class="stat-value" th:text="'$' + ${#numbers.formatDecimal(totalValue, 1, 2)}">$0</div>
        <div class="stat-label">💰 Total Inventory Value</div>
    </div>
    
    <div class="stat-card">
        <div class="stat-value" th:text="'$' + ${#numbers.formatDecimal(averagePrice, 1, 2)}">$0</div>
        <div class="stat-label">📈 Average Price</div>
    </div>
    
    <div class="stat-card warning">
        <div class="stat-value" th:text="${#lists.size(lowStockProducts)}">0</div>
        <div class="stat-label">⚠️ Low Stock Items</div>
    </div>
</div>
```

**Category Bar Chart:**
```html
<div class="bar-chart">
    <div th:each="category : ${categories}" class="bar-item">
        <span class="bar-label" th:text="${category}">Category</span>
        <div class="bar-container">
            <div class="bar-fill" 
                 th:style="'width:' + ${productsByCategory[category] * 100 / totalProducts} + '%'">
            </div>
        </div>
        <span class="bar-value" th:text="${productsByCategory[category]}">0</span>
    </div>
</div>
```

**Low Stock Alerts Table:**
```html
<table>
    <thead>
        <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Category</th>
            <th>Quantity</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="product : ${lowStockProducts}" class="low-stock-row">
            <td th:text="${product.productCode}">P001</td>
            <td th:text="${product.name}">Product</td>
            <td th:text="${product.category}">Category</td>
            <td th:text="${product.quantity}">5</td>
        </tr>
    </tbody>
</table>
```

**Dashboard Layout:**
```
┌─────────────────────────────────────────────────────────────────────┐
│                   📊 Product Statistics Dashboard                    │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │   150    │  │ $45,230  │  │ $301.53  │  │    8     │            │
│  │ Products │  │  Value   │  │ Avg Price│  │Low Stock │            │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │
├─────────────────────────────────────────────────────────────────────┤
│  📂 Products by Category                                            │
│  Electronics  ████████████████████  45                              │
│  Furniture    ██████████████  35                                    │
│  Clothing     ████████████  30                                      │
├─────────────────────────────────────────────────────────────────────┤
│  ⚠️ Low Stock Alerts (Quantity < 10)                                │
│  ┌──────┬────────────┬─────────────┬──────────┐                    │
│  │ P001 │ Widget A   │ Electronics │    3     │                    │
│  │ P015 │ Gadget B   │ Electronics │    5     │                    │
│  └──────┴────────────┴─────────────┴──────────┘                    │
├─────────────────────────────────────────────────────────────────────┤
│  🕐 Recent Products (Last 5 Added)                                  │
│  ┌──────┬────────────┬─────────────┬─────────────────┐             │
│  │ P150 │ New Item   │ Clothing    │ Dec 05, 2025    │             │
│  └──────┴────────────┴─────────────┴─────────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
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

## How to Run

1. Ensure MySQL is running with database `product_management`
2. Configure `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/product_management
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access the application at: http://localhost:8080/products
5. Access the dashboard at: http://localhost:8080/dashboard
