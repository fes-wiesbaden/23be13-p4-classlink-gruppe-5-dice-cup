package de.dicecup.classlink.features.users.domain.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);

    @Query("select s from Student s where s.clazz.id = :classId")
    List<Student> findByClassId(@Param("classId") UUID classId);
}
