package ru.work.Lab7.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.work.Lab7.ExceptionHandlers.MyNotFoundException;
import ru.work.Lab7.model.Student;
import static org.junit.Assert.*;

@DataJpaTest
@ActiveProfiles("test")
public class StudentRepositoryTest {

	@Autowired
	private StudentsRepository studentsRepository;

	@Test
	void IsEmptyTest() {
		assertEquals(0, studentsRepository.findAll().stream().count());
	}

	@Test
	void saveStudentTest() {
		Student student = new Student("Ivan", "Ivanov", 50);
		studentsRepository.save(student);
		assertTrue(studentsRepository.findById(student.getId()).isPresent());
	}

	@Test
	void deleteStudentTest() {
		Student student = new Student("Ivan", "Ivanov", 50);
		studentsRepository.save(student);

		studentsRepository.deleteById(student.getId());
		assertFalse(studentsRepository.findById(student.getId()).isPresent());
	}

	@Test
	void changeMoneyTest(){
		int moneyStart = 50;
		Student student = new Student("Ivan", "Ivanov", moneyStart);
		studentsRepository.save(student);

		student.setMoney(moneyStart+20);
		studentsRepository.save(student);

		Student student1 = studentsRepository.findById(student.getId()).orElseThrow(() -> new MyNotFoundException("No such student"));
		assertEquals(moneyStart+20, student1.getMoney());
	}

}