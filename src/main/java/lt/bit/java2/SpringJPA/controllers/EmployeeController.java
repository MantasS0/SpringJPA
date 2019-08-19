package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.entities.Title;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
class EmployeeController {

    final private EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }


    @GetMapping("/{id}")
    public String getEmployee(@PathVariable int id, ModelMap map) {
        try {
            Employee employee = employeeRepository.getOne(id);
            map.addAttribute("employee", employee);
            return "employee";
        } catch (EntityNotFoundException e) {
            String intent = "Find";
            map.addAttribute("intent", intent);
            map.addAttribute("exception", e);
            map.addAttribute("id", id);
            return "employee-error";
        }
    }

    @GetMapping("/add-form")
    public String addEmployeeForm(Employee employee) {
        return "employee-add";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid Employee employee, BindingResult result, ModelMap map) {
        if (result.hasErrors()) {
            return "employee-add";
        }

        employee.getTitles().forEach(t -> t.setEmployee(employee));
        employeeRepository.save(employee);

        return "redirect:/employee";
    }

    @RequestMapping(value = "/{id}/update", params = {"addTitle"})
    public String addTitle(@PathVariable int id, Employee employee, BindingResult bindingResult) {
        employee.getTitles().add(new Title());
        return "employee-edit";
    }

    @RequestMapping(value = "/{id}/update", params = {"removeTitle"})
    public String removeTitle(@PathVariable int id, Employee employee, BindingResult bindingResult, final HttpServletRequest req) {
        final Integer titleIndex = Integer.valueOf(req.getParameter("removeTitle"));
        if (employee.getTitles().contains(employee.getTitles().get(titleIndex))) {
            employee.getTitles().remove(titleIndex.intValue());
        }
        return "employee-edit";
    }


    @GetMapping("/{id}/edit")
    public String getEmployeeEdit(@PathVariable int id, ModelMap map) {
        try {
            Employee employee = employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            map.addAttribute("employee", employee);
            return "employee-edit";
        } catch (EntityNotFoundException e) {
            String intent = "Edit";
            map.addAttribute("intent", intent);
            map.addAttribute("exception", e);
            map.addAttribute("id", id);
            return "employee-error";
        }
    }

    @PostMapping("/{id}/update")
    public String updateEmployee(@PathVariable("id") int id, @Valid Employee employee, BindingResult result, ModelMap map) {
        if (result.hasErrors()) {
            return "redirect:/employee/" + id + "/edit";
        }

        Optional<Employee> emp = employeeRepository.findById(employee.getEmpNo());
        if (!emp.isPresent()) {
            String intent = "Edit";
            map.addAttribute("intent", intent);
            map.addAttribute("exception", "No employee with employee number '" + id + "' found in the database");
            map.addAttribute("id", id);
            return "employee-error";
        }

        Employee empOrg = emp.get();

        empOrg.setFirstName(employee.getFirstName());
        empOrg.setLastName(employee.getLastName());
        empOrg.setGender(employee.getGender());
        empOrg.setBirthDate(employee.getBirthDate());
        empOrg.setHireDate(employee.getHireDate());

        /*
         *   1. Delete titles with edited primary key fields (primary keys can not be edited, because the DB will not be able to find the items.
         *   Therefore we need to delete these titles and create new ones)
         */
        empOrg.getTitles().removeIf(title ->
                employee.getTitles().stream()
                        .noneMatch(t -> t.getTitle().equals(title.getTitle()) &&
                                t.getFromDate().equals(title.getFromDate()))
        );

        /*
         *   2. Modify titles where only simple data fields were edited (in this case only toDate field)
         */
        empOrg.getTitles().forEach(title -> {
            employee.getTitles().stream()
                    .filter(t1 -> t1.getFromDate().equals(title.getFromDate()) &&
                            t1.getTitle().equals(title.getTitle()) &&
                            !t1.getToDate().equals(title.getToDate()))
                    .findAny()
                    .ifPresent(t2 -> title.setToDate(t2.getToDate()));
        });

        /*
         *   3. Create new titles that are new or that were deleted in the first step of this method
         */
        employee.getTitles().stream()
                .filter(title -> empOrg.getTitles().stream()
                        .noneMatch(t -> t.getFromDate().equals(title.getFromDate()) &&
                                t.getTitle().equals(title.getTitle())))
                .forEach(title -> {
                    title.setEmployee(empOrg);
                    empOrg.getTitles().add(title);
                });
        employeeRepository.save(empOrg);

        return "redirect:/employee";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable("id") int id, ModelMap map) {
        try {
            Optional<Employee> employee = employeeRepository.findById(id);
            if (employee.isPresent()) {
                employeeRepository.delete(employee.get());
            }
        } catch (Exception e) {
            String intent = "Delete";
            map.addAttribute("intent", intent);
            map.addAttribute("id", id);
            map.addAttribute("exception", e);
            return "employee-error";
        }
        return "redirect:/employee";
    }

/*
    @GetMapping
    public String getEmployeeSinglePage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int pageSize,
            @SortDefault(sort = "empNo", direction = Sort.Direction.ASC) Sort sort,
            ModelMap map) {
        Page<Employee> result = employeeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize, sort));

        map.addAttribute("result", result);
        Sort.Order order = sort.iterator().next();
        map.addAttribute("sorting", order);

        HashMap<String, Integer> pageRange = getPaginationRange(pageNumber, result.getTotalPages());
        map.addAttribute("rangeFrom", pageRange.get("from"));
        map.addAttribute("rangeTo", pageRange.get("to"));

        return "employee-list-page";
    }
*/


    @GetMapping
    public String getEmployeeSinglePage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int pageSize,
            @SortDefault(sort = "empNo", direction = Sort.Direction.ASC) Sort sort,
            @RequestParam(name = "criteria", required = false) String criteria,
            ModelMap map) {

        Optional<Page<Employee>> result = Optional.empty();

        if (criteria == null || criteria.isEmpty() || criteria.isBlank()){
            result = Optional.ofNullable(employeeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize, sort)));
//            return "redirect:/employee";
        }else if (criteria.matches(".*\\d.*")) {
            Integer empNo = Integer.parseInt(criteria.replaceAll("^\\D*?(-?\\d+).*$", "$1"));
            if (empNo == 0) {
                return "employee-error";
            } else if (empNo < 0) {
                empNo *= -1;
            }
            result = Optional.ofNullable(employeeRepository.findByEmpNo(empNo, (PageRequest.of(pageNumber - 1, pageSize, sort))));
        } else if (criteria.trim().matches("^[\"'](\\w+\\s{1}\\w+)[\"']$")) {
            String criteria2 = criteria.replaceAll("[\"']", "");
            String[] crit = criteria2.trim().split("\\s+");
            System.out.println(criteria + " -> " + criteria2 + " -> " + crit[0] + " + " + crit[1]);
            result = Optional.ofNullable(employeeRepository.findByFirstNameContainingAndLastNameContaining(crit[0], crit[1], (PageRequest.of(pageNumber - 1, pageSize, sort))));
            if (result.isPresent()){
                if (result.get().isEmpty()){
                    result = Optional.ofNullable(employeeRepository.findByFirstNameContainingAndLastNameContaining(crit[1], crit[0], (PageRequest.of(pageNumber - 1, pageSize, sort))));
                }
            }
        } else {
            System.out.println("reached one word search (else statement)");
            String[] crit = criteria.trim().split("\\s+");
            result = Optional.ofNullable(employeeRepository.findByFirstNameContainingOrLastNameContaining(crit[0], crit[0], (PageRequest.of(pageNumber - 1, pageSize, sort))));
        }
        if (result.isEmpty()) {
            return "employee-error";
        }
        Page<Employee> resultFinal = result.get();
        map.addAttribute("result", resultFinal);
        Sort.Order order = sort.iterator().next();
        map.addAttribute("sorting", order);
        map.addAttribute("criteria", criteria);
        HashMap<String, Integer> pageRange = getPaginationRange(pageNumber, resultFinal.getTotalPages());
        map.addAttribute("rangeFrom", pageRange.get("from"));
        map.addAttribute("rangeTo", pageRange.get("to"));

        return "employee-list-page";
    }


    private HashMap<String, Integer> getPaginationRange(int pageNumber, int pageCount) {
        HashMap<String, Integer> pageRange = new HashMap<>();
        int rangeFrom;
        int rangeTo;
        if (pageNumber == 1) {
            rangeFrom = 1;
            rangeTo = 5;
        } else if (pageNumber == 2) {
            rangeFrom = 1;
            rangeTo = 5;
        } else if (pageNumber == 3) {
            rangeFrom = 1;
            rangeTo = 5;
        } else if (pageNumber == pageCount - 2) {
            rangeFrom = pageCount - 4;
            rangeTo = pageCount;
        } else if (pageNumber == pageCount - 1) {
            rangeFrom = pageCount - 4;
            rangeTo = pageCount;
        } else if (pageNumber == pageCount) {
            rangeFrom = pageCount - 4;
            rangeTo = pageCount;
        } else {
            rangeFrom = pageNumber - 2;
            rangeTo = pageNumber + 2;
        }
        pageRange.put("from", rangeFrom);
        pageRange.put("to", rangeTo);
        return pageRange;
    }


}

