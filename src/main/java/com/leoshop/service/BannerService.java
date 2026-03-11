package com.leoshop.service;

import com.leoshop.model.Banner;
import com.leoshop.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<Banner> getPublicBanners() {
        return bannerRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderBySortOrderAsc();
    }

    public Banner create(Banner banner) {
        return bannerRepository.save(banner);
    }

    public Banner update(Long id, Banner data) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        banner.setTitle(data.getTitle());
        banner.setSubtitle(data.getSubtitle());
        banner.setCtaText(data.getCtaText());
        banner.setCtaLink(data.getCtaLink());
        banner.setImageUrl(data.getImageUrl());
        banner.setBgColor(data.getBgColor());
        banner.setSortOrder(data.getSortOrder());
        banner.setEnabled(data.getEnabled());
        return bannerRepository.save(banner);
    }

    public void delete(Long id) {
        bannerRepository.deleteById(id);
    }

    public Banner toggle(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        banner.setEnabled(!banner.getEnabled());
        return bannerRepository.save(banner);
    }
}
