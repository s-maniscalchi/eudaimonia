package se.eudaimonia;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private OfficeView officeView;
    private DeepWorkView deepWorkView;

    @Override
    public void start(Stage stage) {

        layout = new BorderPane();
        layout.getStyleClass().add("app-root");

        // Header

        roomTitle = new Label();
        roomTitle.getStyleClass().add("heading");

        roomDescription = new Label();
        roomDescription.getStyleClass().add("subtitle");

        roomCounter = new Label();
        roomCounter.getStyleClass().add("secondary-text");

        VBox headerContent = new VBox(
                7,
                roomTitle,
                roomDescription,
                roomCounter
        );

        headerContent.setPadding(new Insets(25, 30, 20, 30));
        headerContent.setMaxWidth(1100);

        StackPane header = new StackPane(headerContent);
        header.setAlignment(Pos.CENTER);

        layout.setTop(header);

        // Footer

        continueButton = new Button();
        continueButton.getStyleClass().add("primary-button");

        continueButton.setOnAction(event -> nextRoom());

        HBox footerContent = new HBox(continueButton);
        footerContent.setAlignment(Pos.CENTER_RIGHT);
        footerContent.setPadding(new Insets(20, 30, 25, 30));
        footerContent.setMaxWidth(1100);

        StackPane footer = new StackPane(footerContent);
        footer.setAlignment(Pos.CENTER);

        layout.setBottom(footer);

        // Initial room

        updateRoom();

        Scene scene = new Scene(layout, 1200, 800);

        // Load global stylesheet

        String css = getClass()
                .getResource("/se/eudaimonia/style.css")
                .toExternalForm();

        scene.getStylesheets().add(css);

        stage.setTitle("Eudaimonia");
        stage.setScene(scene);

        stage.setMinWidth(900);
        stage.setMinHeight(650);

        stage.show();

        // Start in fullscreen

        stage.setFullScreenExitHint(
                "Press ESC to leave fullscreen."
        );

        stage.setFullScreen(true);
    }

    private void nextRoom() {

        if (currentRoom < rooms.length - 1) {
            currentRoom++;
            updateRoom();
        } else {
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

        Node currentView;

        switch (currentRoom) {

            case 0:
                if (galleryView == null) {
                    galleryView = new GalleryView();
                }

                currentView = galleryView;
                break;

            case 1:
                if (salonView == null) {
                    salonView = new SalonView();
                }

                currentView = salonView;
                break;

            case 2:
                if (libraryView == null) {
                    libraryView = new LibraryView();
                }

                currentView = libraryView;
                break;

            case 3:
                if (officeView == null) {
                    officeView = new OfficeView();
                }

                currentView = officeView;
                break;

            case 4:
                if (deepWorkView == null) {
                    deepWorkView = new DeepWorkView();
                }

                currentView = deepWorkView;
                break;

            default:
                currentView = new VBox();
                break;
        }


        StackPane roomContainer = new StackPane(currentView);
        roomContainer.setAlignment(Pos.TOP_CENTER);

        layout.setCenter(roomContainer);

        if (currentRoom == rooms.length - 1) {
            continueButton.setText("Finish session");
        } else {
            continueButton.setText("Continue");
        }
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
