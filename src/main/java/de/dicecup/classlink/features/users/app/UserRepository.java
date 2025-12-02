package de.dicecup.classlink.features.users.app;

import de.dicecup.classlink.features.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
