package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.entities.Title;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import lt.bit.java2.SpringJPA.utility.PaginationRange;
import lt.bit.java2.SpringJPA.utility.Search;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
class EmployeeController {

    final private EmployeeRepository employeeRepository;
    final private Search search;

    public EmployeeController(EmployeeRepository employeeRepository, Search search) {
        this.employeeRepository = employeeRepository;
        this.search = search;
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

    @GetMapping
    public String getEmployeeSinglePage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int pageSize,
            @SortDefault(sort = "empNo", direction = Sort.Direction.ASC) Sort sort,
            @RequestParam(name = "criteria", required = false) String criteria,
            ModelMap map) {

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<Employee> result;

        if (criteria == null || criteria.isEmpty()){
            result = employeeRepository.findAll(pageable);
        }else {
            result = search.doSearch(criteria, pageable);
        }

        map.addAttribute("result", result);
        Sort.Order order = sort.iterator().next();
        map.addAttribute("sorting", order);
        map.addAttribute("criteria", criteria);

        PaginationRange paginationRange = new PaginationRange(pageNumber, result.getTotalPages());
        map.addAttribute("rangeFrom", paginationRange.getRangeFrom());
        map.addAttribute("rangeTo", paginationRange.getRangeTo());

        return "employee-list-page";
    }

}

