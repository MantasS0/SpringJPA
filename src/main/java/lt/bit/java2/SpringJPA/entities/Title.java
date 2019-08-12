package lt.bit.java2.SpringJPA.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "titles")
@Data
@IdClass(TitlePK.class)
public class Title {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emp_no", referencedColumnName = "emp_no", updatable = false, insertable = false)
    @JsonIgnore
    @ToString.Exclude
    private Employee employee;

    @Id
    private String title;

    @Id
    @Column(name = "from_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @Column(name = "to_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;


}
