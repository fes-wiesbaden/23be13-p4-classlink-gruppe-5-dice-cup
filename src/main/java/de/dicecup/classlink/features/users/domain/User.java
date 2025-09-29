package de.dicecup.classlink.features.users.domain;

import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "ux_users_username", columnNames = "username"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String username;

    @NotBlank
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserInfo userInfo;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Admin admin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Teacher teacher;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    private long version;

}