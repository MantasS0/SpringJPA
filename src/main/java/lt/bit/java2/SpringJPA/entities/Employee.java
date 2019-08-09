package lt.bit.java2.SpringJPA.entities;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees")
@NamedEntityGraph(
        name = Employee.Graph_Titles,
        attributeNodes = {
                @NamedAttributeNode("titles")
        }
)
@Data
public class Employee {

    public static final String Graph_Titles = "graph.Employee.titles";

    @Id
    @Column(name = "emp_no")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer empNo;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('M','F')")
    private Gender gender;

    @Column(name = "hire_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "employee",
            fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST,
            orphanRemoval = true
    )
    private List<Title> titles;

}
