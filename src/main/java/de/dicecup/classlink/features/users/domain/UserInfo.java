package de.dicecup.classlink.features.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class UserInfo {
    @Id
    private UUID id;

    @OneToOne @MapsId
    @JoinColumn(name = "user_id")
    private User user;


    @Size(max = 100) private String firstName;
    @Size(max = 100) private String lastName;
    private LocalDate dateOfBirth;
    @Column(unique = true)
    @Email @Size(max = 255) private String email;
    @Size(max = 50) private String phoneNumber;

    @Embedded
    private Address address;

}
