package core;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private final String name;
    private final byte[] encryptedPassword;

    public User(String name, byte[] encryptedPassword) {
        this.name = name;
        this.encryptedPassword = encryptedPassword;
    }

    public String getName() {
        return name;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + "'}";
    }
}
