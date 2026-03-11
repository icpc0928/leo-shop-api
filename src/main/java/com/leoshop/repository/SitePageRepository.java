package com.leoshop.repository;

import com.leoshop.model.SitePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SitePageRepository extends JpaRepository<SitePage, Long> {
    Optional<SitePage> findBySlugAndEnabledTrue(String slug);
    List<SitePage> findAllByOrderByUpdatedAtDesc();
}
