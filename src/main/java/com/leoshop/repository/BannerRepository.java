package com.leoshop.repository;

import com.leoshop.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByEnabledTrueOrderBySortOrderAsc();
    List<Banner> findAllByOrderBySortOrderAsc();
}
