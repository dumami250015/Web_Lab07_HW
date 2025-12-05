package com.example.product_management.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.product_management.entity.Product;
import com.example.product_management.repository.ProductRepository;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // Task 7.1: Sorting
    @Override
    public List<Product> getAllProducts(Sort sort) {
        return productRepository.findAll(sort);
    }
    
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    @Override
    public Product saveProduct(Product product) {
        // Validation logic can go here
        return productRepository.save(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }
    
    // Task 5.3: Search with Pagination
    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable);
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    // Task 5.1: Multi-Criteria Search
    @Override
    public List<Product> advancedSearch(String name, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        // Convert empty strings to null for proper query handling
        String searchName = (name != null && name.trim().isEmpty()) ? null : name;
        String searchCategory = (category != null && category.trim().isEmpty()) ? null : category;
        return productRepository.searchProducts(searchName, searchCategory, minPrice, maxPrice);
    }
    
    // Task 5.2: Category Filter
    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    
    // Task 8.1: Statistics Methods
    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }
    
    @Override
    public long getProductCountByCategory(String category) {
        return productRepository.countByCategory(category);
    }
    
    @Override
    public BigDecimal getTotalInventoryValue() {
        BigDecimal value = productRepository.calculateTotalValue();
        return value != null ? value : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal getAveragePrice() {
        BigDecimal avg = productRepository.calculateAveragePrice();
        return avg != null ? avg : BigDecimal.ZERO;
    }
    
    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
    
    @Override
    public List<Product> getRecentProducts() {
        return productRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
