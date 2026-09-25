package application.persistence;

import application.link.Link;
import application.link.TrackedLink;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Set;

@Repository
public class LinkRepository {
    private final EntityManager entityManager;


    public LinkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public LinkEntity createAndLock(Link link) {
        entityManager.createNativeQuery("""
                INSERT INTO tracked_links (domain, address) VALUES (:domain, :address)
                ON CONFLICT (address) DO NOTHING
                """)
                .setParameter("domain", link.domain())
                .setParameter("address", link.address())
                .executeUpdate();
        return lock(link).orElseThrow();
    }


    public Optional<LinkEntity> lock(Link link) {
        return entityManager.createQuery("SELECT l FROM LinkEntity l WHERE l.address = :address", LinkEntity.class)
                .setParameter("address", link.address())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }


    @Transactional(readOnly = true)
    public Map<String, Long> countActiveByDomain() {
        List<Object[]> counts = entityManager.createQuery("""
                SELECT l.domain, COUNT(l) FROM LinkEntity l
                WHERE EXISTS (SELECT s.id FROM SubscriptionEntity s WHERE s.link = l)
                GROUP BY l.domain
                """, Object[].class).getResultList();
        Map<String, Long> result = new TreeMap<>();
        for (Object[] count : counts) result.put((String) count[0], (Long) count[1]);
        return result;
    }


    @Transactional(readOnly = true)
    public List<TrackedLink> activeLinks(Set<String> hosts, long afterId, int limit) {
        return entityManager.createQuery("""
                SELECT l FROM LinkEntity l WHERE l.domain IN :hosts AND l.id > :cursor
                AND EXISTS (SELECT s.id FROM SubscriptionEntity s WHERE s.link = l) ORDER BY l.id
                """, LinkEntity.class).setParameter("hosts", hosts).setParameter("cursor", afterId)
                .setMaxResults(limit).getResultList().stream()
                .map(link -> new TrackedLink(link.getId(), link.toLink(), link.getTrackingRevision())).toList();
    }
}
