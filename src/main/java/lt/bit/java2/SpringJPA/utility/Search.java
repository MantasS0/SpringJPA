package lt.bit.java2.SpringJPA.utility;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class Search {

    final private EmployeeRepository employeeRepository;

    public Search(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> doSearch(String criteria, Pageable pageable){

        Page<Employee> result = Page.empty();

        if (criteria.matches(".*\\d.*")) {
            int empNo = Integer.parseInt(criteria.replaceAll("^\\D*?(-?\\d+).*$", "$1"));
            if (empNo == 0) {
                return result;
            } else if (empNo < 0) {
                empNo *= -1;
            }
            result = employeeRepository.findByEmpNo(empNo, pageable);
        } else if (criteria.trim().matches("^[\"'](\\w+\\s{1}\\w+)[\"']$")) {
            String criteria2 = criteria.replaceAll("[\"']", "");
            String[] crit = criteria2.trim().split("\\s+");
            System.out.println(criteria + " -> " + criteria2 + " -> " + crit[0] + " + " + crit[1]);
            result = employeeRepository.findByFirstNameContainingAndLastNameContaining(crit[0], crit[1], pageable);
            if (result.isEmpty()){
                result = employeeRepository.findByFirstNameContainingAndLastNameContaining(crit[1], crit[0], pageable);
            }
        } else {
            System.out.println("reached one word search (else statement)");
            String[] crit = criteria.trim().split("\\s+");
            result = employeeRepository.findByFirstNameContainingOrLastNameContaining(crit[0], crit[0], pageable);
        }
        return result;
    }
}
