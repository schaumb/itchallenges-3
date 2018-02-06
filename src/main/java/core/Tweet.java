package core;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Tweet implements Serializable, Comparable<Tweet> {
    private final long timestamp;
    private final String owner;
    private final String message;

    public Tweet(long timestamp, String owner, String message) {
        this.timestamp = timestamp;
        this.owner = owner;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOwner() {
        return owner;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tweet tweet = (Tweet) o;
        return timestamp == tweet.timestamp &&
                Objects.equals(owner, tweet.owner) &&
                Objects.equals(message, tweet.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, owner, message);
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "timestamp=" + timestamp +
                ", owner='" + owner + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public int compareTo(Tweet o) {
        return Comparator.comparingLong(Tweet::getTimestamp).reversed()
                .thenComparing(Tweet::getOwner)
                .thenComparing(Tweet::getMessage).compare(this, o);
    }
}
