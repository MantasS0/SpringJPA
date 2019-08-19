package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
class EmployeeApi {

    final private EmployeeRepository employeeRepository;

    public EmployeeApi(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable int id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.isPresent() ? ResponseEntity.ok(employee.get()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        employee.getTitles().forEach(t -> t.setEmployee(employee));
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeRepository.save(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable int id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            employeeRepository.delete(employee.get());
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<Employee> edit(@RequestBody Employee employee) {
        Optional<Employee> emp = employeeRepository.findById(employee.getEmpNo());
        if (!emp.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(employee);
        }

        Employee empOrg = emp.get();
        empOrg.setFirstName(employee.getFirstName());
        empOrg.setLastName(employee.getLastName());
        empOrg.setGender(employee.getGender());
        empOrg.setBirthDate(employee.getBirthDate());
        empOrg.setHireDate(employee.getHireDate());
        /*
         * Titles can be:
         * 1. modify old one
         * 2. delete old one
         * 3. create new one
         */

        //1. delete old one
        empOrg.getTitles().removeIf(title ->
                employee.getTitles().stream()
                        .noneMatch(t -> t.getTitle().equals(title.getTitle()) &&
                                t.getFromDate().equals(title.getFromDate()))
        );
/*
        Iterator<Title> it = empOrg.getTitles().iterator();
        while (it.hasNext()) {
            Title title = it.next();

            boolean exists = employee.getTitles().stream()
                    .anyMatch(t -> t.getTitle().equals(title.getTitle()) &&
                            t.getFromDate().equals(title.getFromDate()));
            //
            if (!exists) {
                it.remove();
            }
        }
*/

        // 2. modify old one
        empOrg.getTitles().forEach(title -> {
            employee.getTitles().stream()
                    .filter(t1 -> t1.getFromDate().equals(title.getFromDate()) &&
                            t1.getTitle().equals(title.getTitle()) &&
                            !t1.getToDate().equals(title.getToDate()))
                    .findAny()
                    .ifPresent(t2 -> title.setToDate(t2.getToDate()));
        });

        //3. create new one
        employee.getTitles().stream()
                .filter(title -> empOrg.getTitles().stream()
                        .noneMatch(t -> t.getFromDate().equals(title.getFromDate()) &&
                                t.getTitle().equals(title.getTitle())))
                .forEach(title -> {
                    title.setEmployee(empOrg);
                    empOrg.getTitles().add(title);
                });


        return ResponseEntity.ok(employeeRepository.save(emp.get()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Employee>> getEmployeeSinglePage(
            @RequestParam(name = "criteria", required = false, defaultValue = "") String criteria) {
        int pageNumber = 1;
        int pageSize = 5;

        Optional<Page<Employee>> result = Optional.empty();


        if (criteria.matches(".*\\d.*")){
            Integer empNo = Integer.parseInt(criteria.replaceAll("^\\D*?(-?\\d+).*$", "$1"));
            if (empNo==0){
                return ResponseEntity.notFound().build();
            }
            else if (empNo<0){
                empNo *= -1;
            }
            result = Optional.ofNullable(employeeRepository.findByEmpNo(empNo, (PageRequest.of(pageNumber - 1, pageSize))));
        } else if (criteria.trim().contains("%22")){ //criteria.trim().matches("^[\"\'%22]+$")
            //reikia padirbet cia nes neveikia kaip noretusi
            String criteria2 = criteria.replaceAll("%22", "");
            String[] crit = criteria.trim().split(".*[\\h\\+]*.");  //"\\s+"
            System.out.println(criteria + " -> " + criteria2 + " -> " + crit[0] + " + " + crit[1]);
            result = Optional.ofNullable(employeeRepository.findByFirstNameContainingAndLastNameContaining(crit[0], crit[1], (PageRequest.of(pageNumber - 1, pageSize))));
        }else {
            System.out.println("reached one word search (else statement)");
            String[] crit = criteria.trim().split("\\s+");
            result = Optional.ofNullable(employeeRepository.findByFirstNameContainingOrLastNameContaining(crit[0], crit[0], (PageRequest.of(pageNumber - 1, pageSize))));
        }


        return result.isPresent() ? ResponseEntity.ok(result.get()) : ResponseEntity.notFound().build();
    }


}






