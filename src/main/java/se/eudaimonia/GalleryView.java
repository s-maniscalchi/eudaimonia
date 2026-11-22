package se.eudaimonia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GalleryView extends BorderPane {

    private final List<App.GalleryItem> items = new ArrayList<>();

    private final Path dataFolder = Paths.get(
            System.getProperty("user.home"), ".eudaimonia");

    private final Path imageFolder = dataFolder.resolve("images");
    private final Path dataFile = dataFolder.resolve("gallery.dat");

    private GridPane gallery;

    public GalleryView() {
	    getStyleClass().add("room-root");
        try {
            Files.createDirectories(imageFolder);
        } catch (IOException e) {
            showError("Could not create the Gallery folder.");
        }

        loadGallery();
        createView();
    }

    private void createView() {

        gallery = new GridPane();
        gallery.setHgap(20);
        gallery.setVgap(20);
        gallery.setAlignment(Pos.TOP_LEFT);

        refreshGallery();

        Button addImage = new Button("+ Add image");
        Button addNote = new Button("+ Add note");

        styleButton(addImage, true);
        styleButton(addNote, false);

        addImage.setOnAction(event -> addImage());
        addNote.setOnAction(event -> addNote());

        HBox actions = new HBox(12, addImage, addNote);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(25);
        content.getChildren().addAll(actions, gallery);
        content.setPadding(new Insets(5, 0, 20, 0));
        content.setStyle("-fx-background-color: #f8f8f6;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: #f8f8f6;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
        );

        setCenter(scrollPane);
    }

    private void refreshGallery() {

        gallery.getChildren().clear();

        if (items.isEmpty()) {
            Label emptyMessage = new Label(
                    "Your gallery is empty. Add something that inspires you."
            );

            emptyMessage.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #999999;"
            );

            gallery.add(emptyMessage, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (App.GalleryItem item : items) {

            VBox card = new VBox(12);
            card.setPadding(new Insets(15));
            card.setPrefWidth(220);
            card.setMinHeight(210);
            card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #eaeae6;" +
                    "-fx-border-radius: 10;"
            );

            if (item.type.equals("image")) {

                ImageView imageView = new ImageView(
                        new Image(item.content, 190, 140, true, true)
                );

                imageView.setFitWidth(190);
                imageView.setFitHeight(140);
                imageView.setPreserveRatio(true);

                card.getChildren().add(imageView);

            } else {

                Label text = new Label(item.content);
                text.setWrapText(true);
                text.setMaxWidth(190);
                text.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: #333333;"
                );

                card.getChildren().add(text);
            }

            Label type = new Label(item.type.toUpperCase());

            type.setStyle(
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #999999;"
            );

            Button remove = new Button("Remove");
            styleButton(remove, false);

            remove.setOnAction(event -> {
                items.remove(item);
                saveGallery();
                refreshGallery();
            });

            card.getChildren().addAll(type, remove);

            gallery.add(card, column, row);

            column++;

            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private void addImage() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose an image");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg", "*.gif"
                )
        );

        File file = chooser.showOpenDialog(getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            String filename = System.currentTimeMillis()
                    + "_" + file.getName();

            Path destination = imageFolder.resolve(filename);
            Files.copy(file.toPath(), destination);

            items.add(new App.GalleryItem(
                    "image",
                    destination.toUri().toString()
            ));

            saveGallery();
            refreshGallery();

        } catch (IOException e) {
            showError("Could not add the image.");
        }
    }

    private void addNote() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("New inspiration");
        dialog.setHeaderText("Add something to your Gallery");
        dialog.setContentText("Your note:");

        dialog.showAndWait().ifPresent(text -> {

            if (!text.isBlank()) {
                items.add(new App.GalleryItem("note", text));
                saveGallery();
                refreshGallery();
            }
        });
    }

    private void styleButton(Button button, boolean primary) {

        if (primary) {
            button.setStyle(
                    "-fx-background-color: #292929;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 10 18 10 18;" +
                    "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: #eeeeeb;" +
                    "-fx-text-fill: #444444;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 8 14 8 14;" +
                    "-fx-cursor: hand;"
            );
        }
    }

    private void saveGallery() {

        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(dataFile))) {

            out.writeObject(items);

        } catch (IOException e) {
            showError("Could not save the Gallery.");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadGallery() {

        if (!Files.exists(dataFile)) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(dataFile))) {

            items.addAll((List<App.GalleryItem>) in.readObject());

        } catch (IOException | ClassNotFoundException e) {
            showError("Could not load the Gallery.");
        }
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eudaimonia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
