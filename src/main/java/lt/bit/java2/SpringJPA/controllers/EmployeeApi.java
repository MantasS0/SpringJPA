package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
class EmployeeApi{

    final EmployeeRepository employeeRepository;

    public EmployeeApi(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable int id){
        Optional<Employee> employee = employeeRepository.findById(id);
                return employee.isPresent() ? ResponseEntity.ok(employee.get()) : ResponseEntity.notFound().build();
    }

    @PostMapping()
    public ResponseEntity<Employee> create(@RequestBody Employee employee){
        //TODO save in DB
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeRepository.save(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable int id){
        Optional<Employee> employee = employeeRepository.findById(id);
        if(employee.isPresent()){
            employeeRepository.delete(employee.get());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.notFound().build();
    }


}
