package api;

import core.Tweet;
import core.User;
import db.IDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TransactionHandler implements ITransaction {
    private final IDatabase database;

    public TransactionHandler(IDatabase database) {
        this.database = database;
    }

    private byte[] encryptPassword(String string) {
        // it can be easy to hack...
        try {
            return MessageDigest.getInstance("MD5").digest(string.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean createAccount(String userName, String password) {
        if (userName.contains("@") ||
                database.getUsers().contains(new User(userName, null)))
            return false;

        database.getUsers().add(new User(userName, encryptPassword(password)));
        return true;
    }

    @Override
    public User logIn(String userName, String password) {
        User user = database.getUsers().stream()
                .filter(userL -> Objects.equals(userL.getName(), userName))
                .findFirst().orElse(null);

        if (user != null && password != null && Arrays.equals(user.getEncryptedPassword(), encryptPassword(password))) {
            return user;
        }

        return null;
    }

    @Override
    public boolean follow(User user, String who) {
        if (user == null || who == null || Objects.equals(user.getName(), who) ||
                !database.getUsers().contains(new User(who, null))) {
            return false;
        }
        database.getFollowings().get(user.getName()).add(who);
        return true;
    }

    @Override
    public boolean publish(User user, String tweet) {
        if (user == null || tweet == null || tweet.length() > 250)
            return false;

        database.getTweets().add(new Tweet(System.currentTimeMillis(), user.getName(), tweet));
        return true;
    }

    @Override
    public Set<String> mentionHelper(String startsWith, int limit) {
        return database.getUsers().stream().map(User::getName)
                .filter(s -> s.startsWith(startsWith))
                .sorted().limit(limit)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Tweet> tweets(String user, int from, int max, FilterType... filters) {
        Arrays.sort(filters);
        boolean needOwned = Arrays.binarySearch(filters, FilterType.FILTERED_OWNED) < 0;
        boolean needFollowing = Arrays.binarySearch(filters, FilterType.FILTERED_FOLLOWING) < 0;
        boolean needMentioned = Arrays.binarySearch(filters, FilterType.FILTERED_MENTIONED) < 0;

        Set<String> follows = database.getFollowings().get(user);
        return database.getTweets().stream()
                .filter(tweet -> (needOwned && Objects.equals(tweet.getOwner(), user)) ||
                        (needFollowing && follows.contains(tweet.getOwner())) ||
                        (needMentioned && tweet.getMessage().contains("@" + user + " ")))
                .skip(from).limit(max).collect(Collectors.toList());
    }
}
