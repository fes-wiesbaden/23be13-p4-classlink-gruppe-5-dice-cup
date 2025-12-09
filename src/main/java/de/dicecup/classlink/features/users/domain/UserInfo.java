package de.dicecup.classlink.features.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Size(max = 100)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 100)
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    @Email
    @Size(max = 255)
    private String email;
}
