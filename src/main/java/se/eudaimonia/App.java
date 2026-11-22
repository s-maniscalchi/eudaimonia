package se.eudaimonia;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private Label roomTitle;
    private Label roomCounter;
    private Button continueButton;

    @Override
    public void start(Stage stage) {

        roomTitle = new Label();
        roomCounter = new Label();

        continueButton = new Button("Continue");
        continueButton.setOnAction(event -> nextRoom());

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
            roomCounter,
            roomTitle,
            continueButton
        );

        Scene scene = new Scene(layout, 800, 500);

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

        if (currentRoom == rooms.length - 1) {
            continueButton.setText("Finish session");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
