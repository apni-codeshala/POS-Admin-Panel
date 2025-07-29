package org.example.chronoadmin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.chronoadmin.db.AdminDatabaseInitializer;

import java.io.IOException;

public class AdminApplication extends Application {

    static {
        // Force JavaFX platform initialization
        System.setProperty("javafx.platform", "desktop");
        System.setProperty("prism.verbose", "true");
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            System.out.println("Starting AdminChronoPos application...");
            System.out.println("JavaFX Platform initialized: " + Platform.isFxApplicationThread());

            // Initialize database
            System.out.println("Initializing database...");
            AdminDatabaseInitializer.initialize();
            System.out.println("Database initialized successfully");

            // Load FXML
            System.out.println("Loading FXML...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_main.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found: /views/admin_main.fxml");
                throw new RuntimeException("FXML file not found");
            }
            Scene scene = new Scene(loader.load());
            System.out.println("FXML loaded successfully");

            // Setup stage
            stage.setTitle("ChronoPos Admin - Scratch Card & License Management");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            System.out.println("Application started successfully");

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("AdminChronoPos main method called with args: " + java.util.Arrays.toString(args));
            System.out.println("Java version: " + System.getProperty("java.version"));
            System.out.println("JavaFX version: " + System.getProperty("javafx.version"));
            System.out.println("Operating system: " + System.getProperty("os.name"));

            // Ensure JavaFX is available
            try {
                Class.forName("javafx.application.Application");
                System.out.println("JavaFX Application class found");
            } catch (ClassNotFoundException e) {
                System.err.println("JavaFX not found in classpath!");
                e.printStackTrace();
                System.exit(1);
            }

            launch(args);
        } catch (Exception e) {
            System.err.println("Error in main method: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
