package com.example.product_management.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.product_management.entity.Product;

public interface ProductService {
    
    List<Product> getAllProducts();
    
    // Task 7.1: Sorting
    List<Product> getAllProducts(Sort sort);
    
    Optional<Product> getProductById(Long id);
    
    Product saveProduct(Product product);
    
    void deleteProduct(Long id);
    
    List<Product> searchProducts(String keyword);
    
    // Task 5.3: Search with Pagination
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    List<Product> getProductsByCategory(String category);
    
    // Task 5.1: Multi-Criteria Search
    List<Product> advancedSearch(String name, String category, BigDecimal minPrice, BigDecimal maxPrice);
    
    // Task 5.2: Category Filter
    List<String> getAllCategories();
    
    // Task 8.1: Statistics Methods
    long getTotalProductCount();
    
    long getProductCountByCategory(String category);
    
    BigDecimal getTotalInventoryValue();
    
    BigDecimal getAveragePrice();
    
    List<Product> getLowStockProducts(int threshold);
    
    List<Product> getRecentProducts();
}
