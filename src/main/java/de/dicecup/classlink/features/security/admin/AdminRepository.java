package de.dicecup.classlink.features.security.admin;

import de.dicecup.classlink.features.users.domain.roles.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

}
