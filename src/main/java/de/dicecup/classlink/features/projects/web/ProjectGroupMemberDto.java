package de.dicecup.classlink.features.projects.web;

import de.dicecup.classlink.features.projects.MemberRole;
import de.dicecup.classlink.features.projects.ProjectGroupMember;

import java.util.UUID;

public record ProjectGroupMemberDto(
        UUID id,
        UUID groupId,
        UUID studentId,
        MemberRole role
) {
    public static ProjectGroupMemberDto from(ProjectGroupMember member) {
        return new ProjectGroupMemberDto(
                member.getId(),
                member.getProjectGroup() != null ? member.getProjectGroup().getId() : null,
                member.getStudent() != null ? member.getStudent().getId() : null,
                member.getRole()
        );
    }
}
