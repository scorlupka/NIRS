package ru.work.Lab7.service;

import org.springframework.stereotype.Service;
import ru.work.Lab7.DTO.StudentDTO;
import ru.work.Lab7.model.Student;

import java.util.List;

@Service
public interface UserServiceInter {
    Student createStudent(StudentDTO studentDTO);
    void changeMoney(int id, int diff);
    void delete(int id);
    List<Student> findAll();
}
