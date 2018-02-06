package db;

public class DataNotLoadedYetException extends RuntimeException {
    public DataNotLoadedYetException() {
    }

    public DataNotLoadedYetException(String message) {
        super(message);
    }

    public DataNotLoadedYetException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotLoadedYetException(Throwable cause) {
        super(cause);
    }
}
