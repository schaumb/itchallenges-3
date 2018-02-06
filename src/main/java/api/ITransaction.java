package api;

import core.Tweet;
import core.User;

import java.util.List;
import java.util.Set;

public interface ITransaction {
    boolean createAccount(String userName, String password);

    User logIn(String userName, String password);

    boolean follow(User user, String who);

    boolean publish(User user, String tweet);

    Set<String> mentionHelper(String startsWith, int limit);

    List<Tweet> tweets(String user, int from, int max, FilterType... filters);

    enum FilterType {
        FILTERED_OWNED,
        FILTERED_FOLLOWING,
        FILTERED_MENTIONED
    }
}
