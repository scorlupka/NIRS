package ru.work.Lab7.Aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.work.Lab7.ExceptionHandlers.MyNotFoundException;
import ru.work.Lab7.repository.StudentsRepository;

import java.util.Optional;

@Component
@Aspect
public class DeleteAspect {

    @Autowired
    private StudentsRepository studentsRepository;

    @Before("execution(void ru.work.Lab7.service.impl.UserService.delete(int)) && args(id)")
    public void checkStudentAspect(int id) {

        Optional<ru.work.Lab7.model.Student> student = studentsRepository.findById(id);

        int studentId = student
                .map(ru.work.Lab7.model.Student::getId)
                .orElseThrow(() -> new MyNotFoundException("No such student"));
    }
}