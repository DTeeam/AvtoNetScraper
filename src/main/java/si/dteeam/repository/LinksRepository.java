package si.dteeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import si.dteeam.entity.Link;

import java.util.List;
import java.util.Optional;

public interface LinksRepository  extends JpaRepository<Link, Integer> {
    @Query("SELECT l FROM Link l WHERE l.subscriber.chatID = :chatId")
    List<Link> findLinksByChatId(@Param("chatId") Long chatId);

    Link findLinksById(Long id);

    @Modifying
    @Query("UPDATE Link l SET l.isSubscribed = :subscribe WHERE l.subscriber.id = :subscriberId AND l.url = :linkUrl")
    @Transactional
    int setSubscribeToLink(Long subscriberId, String linkUrl, boolean subscribe);

    //@Query("SELECT l FROM Link l LEFT JOIN FETCH l.vehicles WHERE l.user.id = :userId AND l.url IS NULL")
    Optional<Link> findBySubscriberIdAndUrlIsNull(Long subscriberId);

    Link findLinkBySubscriberIdAndUrl(Long subscriberId, String url);
}
