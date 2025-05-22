package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.dteeam.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
