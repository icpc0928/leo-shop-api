package com.leoshop.controller;

import com.leoshop.model.TeamMember;
import com.leoshop.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    // Public
    @GetMapping("/api/team-members")
    public ResponseEntity<List<TeamMember>> getPublicMembers() {
        return ResponseEntity.ok(teamMemberService.getPublicMembers());
    }

    // Admin
    @GetMapping("/api/admin/team-members")
    public ResponseEntity<List<TeamMember>> getAllMembers() {
        return ResponseEntity.ok(teamMemberService.getAllMembers());
    }

    @PostMapping("/api/admin/team-members")
    public ResponseEntity<TeamMember> create(@RequestBody TeamMember member) {
        return ResponseEntity.ok(teamMemberService.create(member));
    }

    @PutMapping("/api/admin/team-members/{id}")
    public ResponseEntity<TeamMember> update(@PathVariable Long id, @RequestBody TeamMember member) {
        return ResponseEntity.ok(teamMemberService.update(id, member));
    }

    @DeleteMapping("/api/admin/team-members/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamMemberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/team-members/{id}/toggle")
    public ResponseEntity<TeamMember> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(teamMemberService.toggle(id));
    }
}
