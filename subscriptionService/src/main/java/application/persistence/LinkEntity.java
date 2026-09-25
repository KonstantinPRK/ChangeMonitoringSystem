package application.persistence;

import application.link.Link;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "tracked_links",
        uniqueConstraints = @UniqueConstraint(name = "uk_tracked_links_address", columnNames = "address"),
        indexes = @Index(name = "ix_tracked_links_domain", columnList = "domain")
)
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 253)
    private String domain;

    @Column(nullable = false, length = 2048)
    private String address;

    @Column(name = "tracking_revision", nullable = false)
    private long trackingRevision;


    protected LinkEntity() {
    }


    public long getId() {
        return id;
    }


    public long getTrackingRevision() {
        return trackingRevision;
    }


    public void advanceTrackingRevision() {
        trackingRevision++;
    }


    public Link toLink() {
        return new Link(domain, address);
    }
}
