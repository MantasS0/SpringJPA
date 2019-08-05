package lt.bit.java2.SpringJPA.repositories;

import lt.bit.java2.SpringJPA.entities.Title;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleRepository extends JpaRepository<Title, Integer> {
}
