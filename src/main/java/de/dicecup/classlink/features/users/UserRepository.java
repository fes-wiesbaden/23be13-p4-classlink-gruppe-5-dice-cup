package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.users.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserInfoEmail(String email);
    Optional<User> findByUsername(String username);
    @EntityGraph(attributePaths = { "userInfo", "admin", "teacher", "student" })
    @Query("select u from User u")
    List<User> findAllWithRolesAndInfo();
}
