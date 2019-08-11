package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.entities.Title;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/employee")
class EmployeeController {

    @Autowired
    EmployeeRepository employeeRepository;


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
    public String addEmployeeForm(Employee employee){
        return "employee-add";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid Employee employee, BindingResult result, ModelMap map){
        if (result.hasErrors()){
            return "employee-add";
        }

        Title title = new Title();
        title.setTitle(employee.getTitles().get(0).getTitle());
        title.setFromDate(employee.getHireDate());
        title.setToDate(LocalDate.of(9999,1,1));
        title.setEmployee(employee);

        employee.getTitles().clear();

        employee.getTitles().add(title);

        employeeRepository.save(employee);
        return "redirect:/employee";
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
            employee.setEmpNo(id);
            return "employee-edit";
        }
        List<Title> titleList = new ArrayList<>(employee.getTitles());
        /**
         * titleList comes with employee set as null, because there is a stackOverflowException thrown (since it goes into infinite loop)
         */
        titleList.forEach(t -> t.setEmployee(employee));
        employee.getTitles().clear();
        employee.getTitles().addAll(titleList);
        employeeRepository.save(employee);
        return "redirect:/employee";
    }

    @GetMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable("id") int id, ModelMap map) {
        try {
            Employee employee = employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            employeeRepository.delete(employee);
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
//            @PageableDefault(size = 10, sort = "empNo") Pageable pageable,
            @RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int pageSize,
            @SortDefault(sort="empNo",direction = Sort.Direction.ASC) Sort sort,
            ModelMap map) {
        Page<Employee> result = employeeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize,sort));

        map.addAttribute("result", result);

        HashMap<String, Integer> pageRange = getPaginationRange(pageNumber, result.getTotalPages());
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

