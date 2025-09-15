package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.dteeam.entity.Subscriber;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Subscriber, Long> {
    //@Query("SELECT u FROM Subscriber u WHERE u.chatID = :chatID")
    @Query("SELECT s FROM Subscriber s LEFT JOIN FETCH s.url WHERE s.chatID = :chatID")
    Optional<Subscriber> findByChatID(Long chatID);

}
