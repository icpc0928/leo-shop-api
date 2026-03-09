package com.leoshop.service;

import com.leoshop.model.Category;
import com.leoshop.repository.CategoryRepository;
import com.leoshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public Category createCategory(Category category) {
        // Check if name already exists
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category name already exists");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category categoryData) {
        Category existing = getCategoryById(id);
        
        // Check if new name conflicts with another category
        if (!existing.getName().equals(categoryData.getName())) {
            categoryRepository.findByName(categoryData.getName()).ifPresent(c -> {
                if (!c.getId().equals(id)) {
                    throw new RuntimeException("Category name already exists");
                }
            });
        }
        
        existing.setName(categoryData.getName());
        existing.setDescription(categoryData.getDescription());
        existing.setSortOrder(categoryData.getSortOrder());
        existing.setActive(categoryData.getActive());
        
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // Check if any products use this category
        long productCount = productRepository.countByCategory(category.getName());
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category: " + productCount + " products are using it");
        }
        
        categoryRepository.delete(category);
    }
}
