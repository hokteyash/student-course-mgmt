package com.hokte.student_mgmt.repo;

import com.hokte.student_mgmt.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {
    Optional<Student> finByUserEmail(String email);
}
