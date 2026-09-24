package application;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LinkToUsersDataBase {
    ConcurrentHashMap<Link, Set<User>>  linkSubscribers; // много вопросов к этой структуре пока попозже

    public Set<User> getLinkSubscribers(Link link) {
        return linkSubscribers.get(link);
    }
}
