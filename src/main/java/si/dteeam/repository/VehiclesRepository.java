package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.dteeam.entity.Vehicles;

import java.util.List;

public interface VehiclesRepository extends JpaRepository<Vehicles, Long> {
    @Query("SELECT v.url FROM Vehicles v")
    List<String> findAllUrls();
}
