package se.eudaimonia;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    @Override
    public void start(Stage stage) {

        roomTitle = new Label();
        roomDescription = new Label();
        roomCounter = new Label();

        continueButton = new Button("Continue");
        continueButton.setOnAction(event -> nextRoom());

        layout = new BorderPane();
        layout.setPadding(new Insets(35));

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
            roomCounter,
            roomTitle,
            roomDescription
        );

        layout.setTop(header);

        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getChildren().add(continueButton);

        layout.setBottom(footer);

        Scene scene = new Scene(layout, 900, 600);

        stage.setTitle("Eudaimonia");
        stage.setScene(scene);

        updateRoom();

        stage.show();
    }

    private void nextRoom() {

        if (currentRoom < rooms.length - 1) {
            currentRoom++;
            updateRoom();
        }
    }

    private void updateRoom() {

        roomTitle.setText(rooms[currentRoom]);
        roomCounter.setText(
            String.format("%02d / %02d", currentRoom + 1, rooms.length)
        );
        roomDescription.setText(descriptions[currentRoom]);

        continueButton.setText(
            currentRoom == rooms.length - 1
                ? "Finish session"
                : "Continue"
        );

        if (currentRoom == 0) {
            showGallery();
        } else {
            layout.setCenter(createRoomContent());
        }
    }

    private void showGallery() {

        GridPane gallery = new GridPane();
        gallery.setAlignment(Pos.CENTER);
        gallery.setHgap(20);
        gallery.setVgap(20);

        for (int i = 0; i < 3; i++) {

            VBox card = new VBox(10);
            card.setAlignment(Pos.CENTER);
            card.setPrefSize(200, 170);
            card.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-background-radius: 8;"
            );

            Label cardLabel = new Label("Inspiration " + (i + 1));
            card.getChildren().add(cardLabel);

            gallery.add(card, i, 0);
        }

        Button addButton = new Button("+ Add inspiration");
        addButton.setOnAction(event -> {
            // Adding inspiration will be implemented later.
        });

        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(gallery, addButton);

        layout.setCenter(content);
    }

    private VBox createRoomContent() {

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);

        return content;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
