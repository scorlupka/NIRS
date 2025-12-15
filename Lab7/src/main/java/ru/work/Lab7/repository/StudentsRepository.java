package ru.work.Lab7.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.work.Lab7.model.Student;
import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findById(Integer id);
}