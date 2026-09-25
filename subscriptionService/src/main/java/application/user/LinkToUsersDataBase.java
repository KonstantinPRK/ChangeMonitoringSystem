package application.user;

import application.link.Link;
import application.persistence.UserEntity;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LinkToUsersDatabase {
    private final EntityManager entities;


    public LinkToUsersDatabase(EntityManager entities) {
        this.entities = entities;
    }


    public SubscriberBatch read(Link link, long afterUserId, int limit) {
        List<UserEntity> users = entities.createQuery("""
                SELECT s.user FROM SubscriptionEntity s
                WHERE s.link.address = :address AND s.user.id > :cursor ORDER BY s.user.id
                """, UserEntity.class).setParameter("address", link.address()).setParameter("cursor", afterUserId)
                .setMaxResults(limit).getResultList();
        long cursor = users.isEmpty() ? afterUserId : users.get(users.size() - 1).getId();
        return new SubscriberBatch(users.stream().map(UserEntity::toUser).toList(), cursor);
    }
}
