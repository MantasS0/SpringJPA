package lt.bit.java2.SpringJPA.repositories;

import lt.bit.java2.SpringJPA.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
