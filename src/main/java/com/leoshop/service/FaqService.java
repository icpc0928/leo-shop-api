package com.leoshop.service;

import com.leoshop.model.Faq;
import com.leoshop.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<Faq> getPublicFaqs() {
        return faqRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    public List<Faq> getAllFaqs() {
        return faqRepository.findAllByOrderBySortOrderAsc();
    }

    public Faq create(Faq faq) {
        return faqRepository.save(faq);
    }

    public Faq update(Long id, Faq data) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setQuestion(data.getQuestion());
        faq.setAnswer(data.getAnswer());
        faq.setSortOrder(data.getSortOrder());
        faq.setEnabled(data.getEnabled());
        return faqRepository.save(faq);
    }

    public void delete(Long id) {
        faqRepository.deleteById(id);
    }

    public Faq toggle(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setEnabled(!faq.getEnabled());
        return faqRepository.save(faq);
    }
}
