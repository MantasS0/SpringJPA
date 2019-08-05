package lt.bit.java2.SpringJPA.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TitlePK implements Serializable {

    private Employee employee;

    private String title;

    private LocalDate fromDate;

}
