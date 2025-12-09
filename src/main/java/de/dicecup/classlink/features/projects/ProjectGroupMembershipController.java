package de.dicecup.classlink.features.projects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/project-groups")
public class ProjectGroupMembershipController {

    private final ProjectGroupMembershipService membershipService;

    public ProjectGroupMembershipController(ProjectGroupMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Operation(summary = "Add student to project group")
    @ApiResponse(responseCode = "204", description = "Student assigned")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/{projectGroupId}/members/{studentId}")
    public ResponseEntity<Void> addMember(@PathVariable UUID projectGroupId, @PathVariable UUID studentId) {
        membershipService.addMember(projectGroupId, studentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove student from project group")
    @ApiResponse(responseCode = "204", description = "Student removed")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/{projectGroupId}/members/{studentId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectGroupId, @PathVariable UUID studentId) {
        membershipService.removeMember(projectGroupId, studentId);
        return ResponseEntity.noContent().build();
    }
}
