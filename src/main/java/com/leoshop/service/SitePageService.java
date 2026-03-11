package com.leoshop.service;

import com.leoshop.model.SitePage;
import com.leoshop.repository.SitePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SitePageService {

    private final SitePageRepository sitePageRepository;

    public SitePage getBySlug(String slug) {
        return sitePageRepository.findBySlugAndEnabledTrue(slug)
                .orElseThrow(() -> new RuntimeException("Page not found"));
    }

    public List<SitePage> getAllPages() {
        return sitePageRepository.findAllByOrderByUpdatedAtDesc();
    }

    public SitePage create(SitePage page) {
        return sitePageRepository.save(page);
    }

    public SitePage update(Long id, SitePage data) {
        SitePage page = sitePageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        page.setSlug(data.getSlug());
        page.setTitle(data.getTitle());
        page.setSubtitle(data.getSubtitle());
        page.setContent(data.getContent());
        page.setMetaDescription(data.getMetaDescription());
        page.setEnabled(data.getEnabled());
        return sitePageRepository.save(page);
    }

    public void delete(Long id) {
        sitePageRepository.deleteById(id);
    }

    public SitePage toggle(Long id) {
        SitePage page = sitePageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        page.setEnabled(!page.getEnabled());
        return sitePageRepository.save(page);
    }
}
