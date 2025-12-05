package com.example.product_management.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.product_management.entity.Product;
import com.example.product_management.service.ProductService;

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
        model.addAttribute("totalValue", totalValue != null ? totalValue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // Average product price
        BigDecimal averagePrice = productService.getAveragePrice();
        model.addAttribute("averagePrice", averagePrice != null ? averagePrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // Low stock alerts (quantity < 10)
        List<Product> lowStockProducts = productService.getLowStockProducts(10);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("lowStockCount", lowStockProducts.size());
        
        // Recent products (last 5 added)
        List<Product> recentProducts = productService.getRecentProducts();
        model.addAttribute("recentProducts", recentProducts);
        
        return "dashboard";
    }
}
