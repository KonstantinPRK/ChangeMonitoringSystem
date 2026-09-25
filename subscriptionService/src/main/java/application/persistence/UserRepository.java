package application.persistence;

import application.user.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final EntityManager entityManager;


    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public UserEntity createAndLock(User user) {
        entityManager.createNativeQuery("""
                INSERT INTO service_users (bot_id, user_id, chat_id)
                VALUES (:botId, :userId, :chatId)
                ON CONFLICT (bot_id, user_id, chat_id) DO NOTHING
                """)
                .setParameter("botId", user.botId())
                .setParameter("userId", user.userId())
                .setParameter("chatId", user.chatId())
                .executeUpdate();
        return lock(user).orElseThrow();
    }


    public Optional<UserEntity> lock(User user) {
        return entityManager.createQuery("""
                SELECT u FROM UserEntity u
                WHERE u.botId = :botId AND u.userId = :userId AND u.chatId = :chatId
                """, UserEntity.class)
                .setParameter("botId", user.botId())
                .setParameter("userId", user.userId())
                .setParameter("chatId", user.chatId())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }
}
