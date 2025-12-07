package de.dicecup.classlink.features.users.domain.roles;

import de.dicecup.classlink.features.users.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
