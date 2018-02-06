package db;

import core.Tweet;
import core.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;

public class FileDatabase implements IDatabase, Closeable, PropertyChangeListener {
    private static final String USER_DATA = "user.obj";
    private static final String TWEET_DATA = "tweet.obj";
    private static final String FOLLOW_DATA = "follows.obj";
    private File loadedDir;
    private UserPersistentSet users;
    private TweetPersistentSet tweets;
    private FollowingMap followings;

    public synchronized void load(File dir) throws IOException, ClassNotFoundException {
        try (ObjectInputStream userDataInput = new ObjectInputStream(new FileInputStream(new File(dir, USER_DATA)))) {
            users = (UserPersistentSet) userDataInput.readObject();
        } catch (FileNotFoundException ignored) {
            users = new UserPersistentSet();
            saveUsers();
        }

        try (ObjectInputStream tweetDataInput = new ObjectInputStream(new FileInputStream(new File(dir, TWEET_DATA)))) {
            tweets = (TweetPersistentSet) tweetDataInput.readObject();
        } catch (FileNotFoundException ignored) {
            tweets = new TweetPersistentSet();
            saveTweets();
        }

        try (ObjectInputStream followDataInput = new ObjectInputStream(new FileInputStream(new File(dir, FOLLOW_DATA)))) {
            followings = (FollowingMap) followDataInput.readObject();
        } catch (FileNotFoundException ignored) {
            followings = new FollowingMap();
            saveFollowings();
        }

        loadedDir = dir;
        users.addChangeListener(this);
        tweets.addChangeListener(this);
        followings.values().stream()
                .filter(s -> s instanceof FollowingPersistentSet)
                .map(s -> (FollowingPersistentSet) s)
                .forEach(f -> f.addChangeListener(this));
    }

    private synchronized void saveUsers() throws IOException {
        try (ObjectOutputStream userDataOutput = new ObjectOutputStream(new FileOutputStream(new File(loadedDir, USER_DATA)))) {
            userDataOutput.writeObject(users);
        }
    }

    private synchronized void saveTweets() throws IOException {
        try (ObjectOutputStream tweetDataOutput = new ObjectOutputStream(new FileOutputStream(new File(loadedDir, TWEET_DATA)))) {
            tweetDataOutput.writeObject(tweets);
        }
    }

    private synchronized void saveFollowings() throws IOException {
        try (ObjectOutputStream followDataOutput = new ObjectOutputStream(new FileOutputStream(new File(loadedDir, FOLLOW_DATA)))) {
            followDataOutput.writeObject(followings);
        }
    }

    @Override
    public Set<User> getUsers() {
        if (users == null) {
            throw new DataNotLoadedYetException();
        }

        return users;
    }

    @Override
    public Set<Tweet> getTweets() {
        if (tweets == null) {
            throw new DataNotLoadedYetException();
        }

        return tweets;
    }

    @Override
    public Map<String, Set<String>> getFollowings() {
        if (followings == null) {
            throw new DataNotLoadedYetException();
        }

        return followings;
    }

    @Override
    public void close() throws IOException {
        saveUsers();
        saveTweets();
        saveFollowings();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            ChangeType changeType = ChangeType.valueOf(evt.getPropertyName());
            switch (changeType) {
                case USER:
                    saveUsers();

                    if (evt.getNewValue() != null && evt.getNewValue() instanceof User) {
                        User user = (User) evt.getNewValue();

                        FollowingPersistentSet following = new FollowingPersistentSet();
                        followings.put(user.getName(), following);
                        following.addChangeListener(this);

                        propertyChange(new PropertyChangeEvent(following,
                                ChangeType.FOLLOW.name(), null, null));
                    }
                    break;
                case TWEET:
                    saveTweets();
                    break;
                case FOLLOW:
                    saveFollowings();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum ChangeType {
        USER,
        TWEET,
        FOLLOW
    }

    private static abstract class PersistentSet<T> extends AbstractSet<T> implements Serializable {
        private final Set<T> container;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        private PersistentSet() {
            this(new HashSet<>());
        }

        private PersistentSet(Set<T> container) {
            this.container = Collections.synchronizedSet(container);
        }

        @Override
        public Iterator<T> iterator() {
            return new PersistentIterator(container.iterator());
        }

        @Override
        public int size() {
            return container.size();
        }

        @Override
        public boolean contains(Object o) {
            return container.contains(o);
        }

        @Override
        public boolean add(T elem) {
            if (container.add(elem)) {
                atChange(null, elem);
                return true;
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (container.remove(o)) {
                /* It is a nasty solution, and not needed yet
                @SuppressWarnings("unchecked")
                T elem = (T) o;
                */
                atChange(/* elem */ null, null);
                return true;
            }
            return false;
        }

        protected abstract ChangeType getFireChangeType();

        private void atChange(T oldElem, T newElem) {
            propertyChangeSupport.firePropertyChange(getFireChangeType().name(), oldElem, newElem);
        }

        void addChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        private class PersistentIterator implements Iterator<T> {
            private final Iterator<T> realIterator;
            private T current;

            private PersistentIterator(Iterator<T> realIterator) {
                this.realIterator = realIterator;
            }

            @Override
            public boolean hasNext() {
                return realIterator.hasNext();
            }

            @Override
            public T next() {
                return current = realIterator.next();
            }

            @Override
            public void remove() {
                realIterator.remove();
                atChange(current, null);
            }
        }
    }

    private static class FollowingPersistentSet extends PersistentSet<String> {
        @Override
        protected ChangeType getFireChangeType() {
            return ChangeType.FOLLOW;
        }
    }

    private static class FollowingMap extends HashMap<String, Set<String>> {
    }

    private static class TweetPersistentSet extends PersistentSet<Tweet> {
        private TweetPersistentSet() {
            super(new TreeSet<>());
        }

        @Override
        protected ChangeType getFireChangeType() {
            return ChangeType.TWEET;
        }
    }

    private static class UserPersistentSet extends PersistentSet<User> {
        @Override
        protected ChangeType getFireChangeType() {
            return ChangeType.USER;
        }
    }
}
