package application;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserToLinksDatabase {
    ConcurrentHashMap<User, Set<Link>> userSubscriptions; // много вопросов к этой структуре пока попозже

}
