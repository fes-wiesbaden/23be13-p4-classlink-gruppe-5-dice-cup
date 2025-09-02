package de.dicecup.classlink.features.users.domain;

import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
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
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserInfo userInfo;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Admin admin;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Student student;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Teacher teacher;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime created_at = OffsetDateTime.now();


}