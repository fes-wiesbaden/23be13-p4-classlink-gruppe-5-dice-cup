package de.dicecup.classlink.features.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Entity
public class UserInfo {
    @Id
    private UUID id;

    @OneToOne @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private User user;

    @Size(max = 100) private String firstName;
    @Size(max = 100) private String lastName;
    @Column(unique = true)
    @Email @Size(max = 255) private String email;
}
