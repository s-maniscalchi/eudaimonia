package se.eudaimonia;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.Serializable;

public class App extends Application {

    private int currentRoom = 0;

    private final String[] rooms = {
            "Gallery",
            "Salon",
            "Library",
            "Office",
            "Deep Work Chamber"
    };

    private final String[] descriptions = {
            "Find inspiration. Explore ideas and discover what is possible.",
            "Think, discuss and develop your ideas.",
            "Gather the knowledge and resources you need.",
            "Organize your work and prepare for execution.",
            "Eliminate distractions. Give your full attention to meaningful work."
    };

    private Label roomTitle;
    private Label roomDescription;
    private Label roomCounter;

    private Button continueButton;

    private BorderPane layout;

    private GalleryView galleryView;
    private SalonView salonView;
    private LibraryView libraryView;

    @Override
    public void start(Stage stage) {

        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #f8f8f6;");

        // Header
        roomTitle = new Label();
        roomTitle.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #292929;"
        );

        roomDescription = new Label();
        roomDescription.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #777777;"
        );

        roomCounter = new Label();
        roomCounter.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #999999;"
        );

        VBox header = new VBox(8,
                roomTitle,
                roomDescription,
                roomCounter
        );

        header.setPadding(new Insets(25, 30, 20, 30));

        layout.setTop(header);

        // Footer
        continueButton = new Button();

        styleButton(continueButton);

        continueButton.setOnAction(event -> nextRoom());

        HBox footer = new HBox(continueButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 30, 25, 30));

        layout.setBottom(footer);

        // Initial room
        updateRoom();

        Scene scene = new Scene(layout, 1000, 700);

        stage.setTitle("Eudaimonia");
        stage.setScene(scene);
        stage.show();
    }

    private void nextRoom() {

        if (currentRoom < rooms.length - 1) {
            currentRoom++;
            updateRoom();
        } else {
            // Session finished
            currentRoom = 0;
            updateRoom();
        }
    }

    private void updateRoom() {

        roomTitle.setText(rooms[currentRoom]);
        roomDescription.setText(descriptions[currentRoom]);

        roomCounter.setText(
                "ROOM " + (currentRoom + 1) + " OF " + rooms.length
        );

        switch (currentRoom) {

            case 0:
                if (galleryView == null) {
                    galleryView = new GalleryView();
                }

                layout.setCenter(galleryView);
                break;

            case 1:
                if (salonView == null) {
                    salonView = new SalonView();
                }

                layout.setCenter(salonView);
                break;

            case 2:
                if (libraryView == null) {
                    libraryView = new LibraryView();
                }

                layout.setCenter(libraryView);
                break;

            default:
                layout.setCenter(new VBox());
                break;
        }

        if (currentRoom == rooms.length - 1) {
            continueButton.setText("Finish session");
        } else {
            continueButton.setText("Continue");
        }
    }

    private void styleButton(Button button) {

        button.setStyle(
                "-fx-background-color: #292929;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 10 22 10 22;" +
                "-fx-cursor: hand;"
        );
    }

    static class GalleryItem implements Serializable {

        private static final long serialVersionUID = 1L;

        final String type;
        final String content;

        GalleryItem(String type, String content) {
            this.type = type;
            this.content = content;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
