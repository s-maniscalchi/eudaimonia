package se.eudaimonia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class DeepWorkView extends BorderPane {

    public DeepWorkView() {

        getStyleClass().add("room-root");

        createView();
    }

    private void createView() {

        Label heading = new Label("Deep Work");
        heading.setFont(Font.font("System", 28));
        heading.getStyleClass().add("heading");

        Label subtitle = new Label(
                "Give your full attention to meaningful work."
        );

        subtitle.getStyleClass().add("subtitle");

        Label message = new Label(
                "Your workspace is ready."
        );

        message.setFont(Font.font("System", 18));
        message.setStyle("-fx-text-fill: #30322E;");

        Label description = new Label(
                "This is where preparation becomes focused execution."
        );

        description.getStyleClass().add("secondary-text");

        VBox content = new VBox(
                16,
                heading,
                subtitle,
                message,
                description
        );

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        setCenter(content);
    }
}
