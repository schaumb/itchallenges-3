package db;

import core.Tweet;
import core.User;

import java.util.Map;
import java.util.Set;

public interface IDatabase {
    /* persistent storage for user data */
    Set<User> getUsers();

    /* persistent storage for tweet data */
    Set<Tweet> getTweets();

    /* persistent storage for following data */
    Map<String, Set<String>> getFollowings();
}
