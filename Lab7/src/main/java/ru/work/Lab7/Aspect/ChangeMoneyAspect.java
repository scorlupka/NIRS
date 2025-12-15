package ru.work.Lab7.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.work.Lab7.ExceptionHandlers.MyBadRequestException;
import ru.work.Lab7.ExceptionHandlers.MyNotFoundException;
import ru.work.Lab7.model.Student;
import ru.work.Lab7.repository.StudentsRepository;

import java.util.Optional;

@Component
@Aspect
public class ChangeMoneyAspect {

    @Autowired
    private StudentsRepository studentsRepository;

    @Around("execution(void ru.work.Lab7.service.impl.UserService.changeMoney(int, int)) && args(id,diff)")
    public void checkChangeMoney(ProceedingJoinPoint joinPoint, int id, int diff) throws Throwable {

        Optional<Student> student = studentsRepository.findById(id);

        int studentId = student
                .map(Student::getId)
                .orElseThrow(() -> new MyNotFoundException("No such student"));

        int studentMoney = student
                .map(Student::getMoney)
                .orElseThrow(() -> new MyNotFoundException("No such student"));

        if(studentId != id){
            throw new MyBadRequestException("You can change only your own money");
        }

        if (studentMoney + diff < 0) {
            throw new MyBadRequestException("Not enough money");
        }


        joinPoint.proceed();
    }
}