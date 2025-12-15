package ru.work.Lab7.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.work.Lab7.DTO.StudentDTO;
import ru.work.Lab7.ExceptionHandlers.MyBadRequestException;
import ru.work.Lab7.ExceptionHandlers.MyNotFoundException;
import ru.work.Lab7.model.Student;
import ru.work.Lab7.repository.StudentsRepository;
import ru.work.Lab7.service.UserServiceInter;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserServiceInter {


    private StudentsRepository studentsRepository;

    @Autowired
    public UserService( StudentsRepository studentsRepository) {
        this.studentsRepository = studentsRepository;
    }

    public List<Student> findAll(){
        return studentsRepository.findAll();
    }

    public Student createStudent(StudentDTO studentDTO) {
        Student newStudent = new Student(
                studentDTO.getName(),
                studentDTO.getLastname(),
                studentDTO.getMoney()
        );
        if(studentDTO.getMoney()<0){
            throw new MyBadRequestException("Money cant be negative");
        }
        return studentsRepository.save(newStudent);
    }

    @Override
    public void delete(int id) {
        Optional<Student> optionalStudent =studentsRepository.findById(id);
        if(optionalStudent.isEmpty()){
            throw new MyNotFoundException("No such Student");
        }
        studentsRepository.deleteById(id);
    };

    public void changeMoney(int id, int diff){
        Optional<Student> optionalStudent =studentsRepository.findById(id);
        if(optionalStudent.isEmpty()){
            throw new MyNotFoundException("No such Student");
        }
        Student student = optionalStudent.get();
        student.setMoney(student.getMoney() + diff);
        studentsRepository.save(student);
    }

}
