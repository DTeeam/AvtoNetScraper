package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.dteeam.entity.Links;

import java.util.List;

public interface LinksRepository  extends JpaRepository<Links, Integer> {
    @Query("SELECT l.url FROM Links l")
    List<String> getLinks();

    @Query("SELECT l FROM Links l WHERE l.user.chatID = :chatId")
    List<Links> findLinksByChatId(@Param("chatId") Long chatId);

    @Query("SELECT l FROM Links l WHERE l.url = :url")
    Links findLinksByUrl(@Param("url") String url);
}
