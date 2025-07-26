package org.example.chronoadmin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.chronoadmin.db.AdminDatabaseInitializer;

import java.io.IOException;

public class AdminApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        AdminDatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_main.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("ChronoPos Admin - Scratch Card & License Management");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
