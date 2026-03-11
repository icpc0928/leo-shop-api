package com.leoshop.repository;

import com.leoshop.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByEnabledTrueOrderBySortOrderAsc();
    List<Faq> findAllByOrderBySortOrderAsc();
}
