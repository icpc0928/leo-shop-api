package com.leoshop.repository;

import com.leoshop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    // All active products (no filter)
    Page<Product> findByActiveTrue(Pageable pageable);

    // Filter by category only
    Page<Product> findByActiveTrueAndCategory(String category, Pageable pageable);

    // Filter by keyword only
    @Query("SELECT p FROM Product p WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByActiveTrueAndNameContaining(@Param("keyword") String keyword, Pageable pageable);

    // Filter by both category and keyword
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category = :category AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByActiveTrueAndCategoryAndNameContaining(@Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    // Admin: all products (include inactive)
    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByCategoryAndNameContaining(@Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();
}
