package lt.bit.java2.SpringJPA.controllers;

import lt.bit.java2.SpringJPA.entities.Employee;
import lt.bit.java2.SpringJPA.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityNotFoundException;

@Controller
@RequestMapping("/employee")
class EmployeeController {

    @Autowired
    EmployeeRepository employeeRepository;

    @GetMapping("/{id}")
    public String getEmployee(@PathVariable int id, ModelMap map) {
        try{
        Employee employee = employeeRepository.getOne(id);
        map.addAttribute("employee", employee);
        return "employee";}
        catch (EntityNotFoundException e){
            map.addAttribute("id", id);
            return "employee-error";
        }
    }

    @GetMapping
    public String getEmployeeSinglePage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int pageSize,
            ModelMap map){
        Page<Employee> result = employeeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        map.addAttribute("result", result);

        int rangeFrom;
        int rangeTo;
        int pageCount = result.getTotalPages();

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

        map.addAttribute("rangeFrom",rangeFrom);
        map.addAttribute("rangeTo",rangeTo);

        return "employee-list-page";
    }

}
