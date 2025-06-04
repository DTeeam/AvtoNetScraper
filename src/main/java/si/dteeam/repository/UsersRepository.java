package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.dteeam.entity.Users;

import java.util.List;

public interface UsersRepository  extends JpaRepository<Users, Long> {
    @Query("SELECT u.chatID FROM Users u")
    List<String> findAllUsers();
}
