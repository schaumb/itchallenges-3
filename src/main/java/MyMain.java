import api.TransactionHandler;
import db.FileDatabase;

import java.io.File;
import java.io.IOException;

public class MyMain {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try (FileDatabase fileDatabase = new FileDatabase()) {
            fileDatabase.load(new File("."));

            TransactionHandler handler = new TransactionHandler(fileDatabase);


        }
    }
}