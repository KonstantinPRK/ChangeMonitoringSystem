package application.persistence;

import application.link.Link;

/**
 * Представляет строку отслеживаемой ссылки, прочитанную из базы данных.
 */
public class LinkEntity {
    private final long id;
    private final String domain;
    private final String address;
    private final long trackingRevision;


    public LinkEntity(long id, String domain, String address, long trackingRevision) {
        this.id = id;
        this.domain = domain;
        this.address = address;
        this.trackingRevision = trackingRevision;
    }


    public long getId() {
        return id;
    }


    public long getTrackingRevision() {
        return trackingRevision;
    }


    public Link toLink() {
        return new Link(domain, address);
    }
}
