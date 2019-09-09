package lt.bit.java2.SpringJPA.repositories;

import lt.bit.java2.SpringJPA.entities.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, Integer>, QuerydslPredicateExecutor {

    @Query
    Page<Employee> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName, Pageable pageable);

    @Query
    Page<Employee> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName, Pageable pageable);

    @Query
    Page<Employee> findByEmpNo(Integer empNo, Pageable pageable);

}
