package de.dicecup.classlink.features.users.domain.roles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<List<Student>> findBySchoolClassId(UUID classId);
}
