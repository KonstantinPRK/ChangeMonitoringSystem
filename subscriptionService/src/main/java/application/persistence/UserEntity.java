package application.persistence;

import application.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "service_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_service_users_identity",
                columnNames = {"bot_id", "user_id", "chat_id"}
        )
)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bot_id", nullable = false, length = 128)
    private String botId;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "chat_id", nullable = false, length = 128)
    private String chatId;

    @Column(name = "subscriptions_revision", nullable = false)
    private long subscriptionsRevision;


    protected UserEntity() {
    }


    public long getId() {
        return id;
    }


    public long getSubscriptionsRevision() {
        return subscriptionsRevision;
    }


    public void advanceSubscriptionsRevision() {
        subscriptionsRevision++;
    }


    public User toUser() {
        return new User(botId, userId, chatId);
    }
}
