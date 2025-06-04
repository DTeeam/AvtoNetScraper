package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.dteeam.entity.Links;

import java.util.List;

public interface LinksRepository  extends JpaRepository<Links, Integer> {
    @Query("SELECT l.url FROM Links l")
    List<String> getLinks();
}
