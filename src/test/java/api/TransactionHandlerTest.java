package api;

import core.Tweet;
import core.User;
import db.FileDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionHandlerTest {
    private File tmp;
    private FileDatabase database;
    private TransactionHandler handler;

    @BeforeEach
    void setUp() throws IOException, ClassNotFoundException {
        tmp = Files.createTempDirectory("tmp").toFile();

        database = new FileDatabase();
        database.load(tmp);

        handler = new TransactionHandler(database);
    }

    @AfterEach
    void tearDown() {

        if (!tmp.delete()) {
            tmp.deleteOnExit();
        }
    }

    @Test
    void createAccount() {
        String name = "asd";
        String pw = "fgh";

        assertTrue(handler.createAccount(name, pw));

        assertEquals(1, database.getUsers().size());
        assertTrue(database.getUsers().contains(new User(name, null)));

        assertEquals(1, database.getFollowings().size());
        assertEquals(name, database.getFollowings().keySet().iterator().next());
        assertEquals(0, database.getFollowings().get(name).size());

        assertFalse(handler.createAccount(name, pw));
        assertEquals(1, database.getUsers().size());

        assertTrue(handler.createAccount(name + name, name));
        assertEquals(2, database.getUsers().size());
    }

    @Test
    void logIn() {
        String name = "asd";
        String pw = "fgh";

        assertTrue(handler.createAccount(name, pw));
        assertNotNull(handler.logIn(name, pw));

        database.getUsers().add(new User(name + name, pw.getBytes()));

        assertNull(handler.logIn(null, null));
        assertNull(handler.logIn(name + name, null));
        assertNull(handler.logIn(name + name, pw));
    }

    @Test
    void follow() {
        String name = "asd";
        String pw = "fgh";

        assertTrue(handler.createAccount(name, pw));
        User user = handler.logIn(name, pw);
        assertNotNull(user);

        database.getUsers().add(new User(name + name, pw.getBytes()));

        assertTrue(handler.follow(user, name + name));

        assertEquals(1, database.getFollowings().get(name).size());
        assertEquals(0, database.getFollowings().get(name + name).size());
        assertEquals(name + name, database.getFollowings().get(name).iterator().next());

        assertFalse(handler.follow(user, name));
        assertFalse(handler.follow(user, null));
        assertFalse(handler.follow(user, ""));

        assertFalse(handler.follow(null, name));
    }

    @Test
    void publish() {
        String name = "asd";
        String pw = "fgh";

        assertTrue(handler.createAccount(name, pw));
        User user = handler.logIn(name, pw);
        assertNotNull(user);

        database.getUsers().add(new User(name + name, pw.getBytes()));

        assertEquals(0, database.getTweets().size());
        assertTrue(handler.publish(user, pw));
        assertEquals(1, database.getTweets().size());
        assertEquals(name, database.getTweets().iterator().next().getOwner());

        assertFalse(handler.publish(user, IntStream.range(1, 1000)
                .mapToObj(Integer::toString).reduce("", String::concat)));

        assertFalse(handler.publish(null, null));
        assertFalse(handler.publish(user, null));
        assertFalse(handler.publish(new User(name + name, null), null));
        assertTrue(handler.publish(new User(name + name, null), "Ahoy"));
        assertEquals(2, database.getTweets().size());
        Iterator<Tweet> iterator = database.getTweets().iterator();
        assertTrue(iterator.next().getTimestamp() > iterator.next().getTimestamp());
    }
}