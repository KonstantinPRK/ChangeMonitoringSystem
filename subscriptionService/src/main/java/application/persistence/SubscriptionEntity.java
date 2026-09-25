package application.persistence;

import application.user.SubscriptionView;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(name = "uk_subscriptions_user_link", columnNames = {"user_id", "link_id"}),
        indexes = @Index(name = "ix_subscriptions_link_user", columnList = "link_id,user_id")
)
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_subscriptions_user"), nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", foreignKey = @ForeignKey(name = "fk_subscriptions_link"), nullable = false)
    private LinkEntity link;

    @ElementCollection
    @CollectionTable(
            name = "subscription_tags",
            joinColumns = @JoinColumn(name = "subscription_id"),
            foreignKey = @ForeignKey(name = "fk_subscription_tags_subscription")
    )
    @OrderColumn(name = "position", nullable = false)
    @Column(name = "tag", nullable = false, length = 256)
    @BatchSize(size = 100)
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "subscription_filters",
            joinColumns = @JoinColumn(name = "subscription_id"),
            foreignKey = @ForeignKey(name = "fk_subscription_filters_subscription")
    )
    @OrderColumn(name = "position", nullable = false)
    @Column(name = "expression", nullable = false, length = 256)
    @BatchSize(size = 100)
    private List<String> filters = new ArrayList<>();


    protected SubscriptionEntity() {
    }

    public SubscriptionEntity(UserEntity user, LinkEntity link, List<String> tags, List<String> filters) {
        this.user = user;
        this.link = link;
        this.tags.addAll(tags);
        this.filters.addAll(filters);
    }


    public boolean replaceMetadata(List<String> tags, List<String> filters) {
        if (this.tags.equals(tags) && this.filters.equals(filters)) return false;
        this.tags.clear();
        this.tags.addAll(tags);
        this.filters.clear();
        this.filters.addAll(filters);
        return true;
    }


    public SubscriptionView toView() {
        return new SubscriptionView(link.toLink(), tags, filters);
    }
}
