package application.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubscriptionRepository {
    private final EntityManager entityManager;


    public SubscriptionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public Optional<SubscriptionEntity> find(long userId, long linkId) {
        return entityManager.createQuery("""
                SELECT s FROM SubscriptionEntity s WHERE s.user.id = :userId AND s.link.id = :linkId
                """, SubscriptionEntity.class)
                .setParameter("userId", userId)
                .setParameter("linkId", linkId)
                .getResultStream()
                .findFirst();
    }


    public void save(SubscriptionEntity subscription) {
        entityManager.persist(subscription);
    }


    public void delete(SubscriptionEntity subscription) {
        entityManager.remove(subscription);
    }


    public long countByLink(long linkId) {
        return entityManager.createQuery("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.link.id = :linkId", Long.class)
                .setParameter("linkId", linkId)
                .getSingleResult();
    }
}
