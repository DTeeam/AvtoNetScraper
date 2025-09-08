package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import si.dteeam.entity.Users;
import si.dteeam.entity.Vehicle;

import java.util.Optional;

public interface VehiclesRepository extends JpaRepository<Vehicle, Long> {
    @Modifying
    @Query("UPDATE Vehicle v SET v.isSubscribed = :subscribe WHERE v.link.user.id = :userId AND v.url = :vehicleUrl")
    @Transactional
    int setSubscribeToVehicle(Long userId, String vehicleUrl, boolean subscribe);
}
