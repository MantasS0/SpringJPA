package lt.bit.java2.SpringJPA.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "titles")
@Data
@IdClass(TitlePK.class)
public class Title {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emp_no", referencedColumnName = "emp_no")
    private Employee employee;

    @Id
    private String title;

    @Id
    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;


}
