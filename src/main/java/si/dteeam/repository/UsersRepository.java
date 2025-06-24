package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.dteeam.entity.Links;
import si.dteeam.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    //@Query("SELECT u FROM Users u WHERE u.chatID = :chatID")
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.url WHERE u.chatID = :chatID")
    Optional<Users> findByChatID(Long chatID);

    void removeUsersByChatID(Long chatID);

   /* @Query("SELECT l FROM Users u JOIN u.url l")
    List<Links> findAllLinks();*/
}
