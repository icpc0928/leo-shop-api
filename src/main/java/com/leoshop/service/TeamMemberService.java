package com.leoshop.service;

import com.leoshop.model.TeamMember;
import com.leoshop.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    public List<TeamMember> getPublicMembers() {
        return teamMemberRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    public List<TeamMember> getAllMembers() {
        return teamMemberRepository.findAllByOrderBySortOrderAsc();
    }

    public TeamMember create(TeamMember member) {
        return teamMemberRepository.save(member);
    }

    public TeamMember update(Long id, TeamMember data) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found"));
        member.setName(data.getName());
        member.setRole(data.getRole());
        member.setImageUrl(data.getImageUrl());
        member.setSortOrder(data.getSortOrder());
        member.setEnabled(data.getEnabled());
        return teamMemberRepository.save(member);
    }

    public void delete(Long id) {
        teamMemberRepository.deleteById(id);
    }

    public TeamMember toggle(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found"));
        member.setEnabled(!member.getEnabled());
        return teamMemberRepository.save(member);
    }
}
