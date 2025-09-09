package de.dicecup.classlink.features.users.app;

import de.dicecup.classlink.features.users.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    boolean existsByEmailIgnoreCase(String email);
}
