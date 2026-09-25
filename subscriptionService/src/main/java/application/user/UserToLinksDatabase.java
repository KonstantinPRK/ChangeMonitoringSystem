package application.user;

import application.persistence.SubscriptionEntity;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserToLinksDatabase {
    private final EntityManager entities;


    public UserToLinksDatabase(EntityManager entities) {
        this.entities = entities;
    }


    public Optional<SubscriptionListVersion> version(User user) {
        return entities.createQuery("""
                SELECT new application.user.SubscriptionListVersion(u.id, u.subscriptionsRevision)
                FROM UserEntity u WHERE u.botId = :bot AND u.userId = :user AND u.chatId = :chat
                """, SubscriptionListVersion.class)
                .setParameter("bot", user.botId()).setParameter("user", user.userId()).setParameter("chat", user.chatId())
                .getResultStream().findFirst();
    }


    public List<SubscriptionView> read(long userId, int offset, int limit) {
        return entities.createQuery("""
                SELECT s FROM SubscriptionEntity s JOIN FETCH s.link
                WHERE s.user.id = :user ORDER BY s.id
                """, SubscriptionEntity.class).setParameter("user", userId).setFirstResult(offset).setMaxResults(limit)
                .getResultList().stream().map(SubscriptionEntity::toView).toList();
    }
}
