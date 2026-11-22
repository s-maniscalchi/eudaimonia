package se.eudaimonia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LibraryView extends BorderPane {

    private final List<LibraryItem> items = new ArrayList<>();

    private final Path dataFolder = Paths.get(
            System.getProperty("user.home"), ".eudaimonia");

    private final Path fileFolder = dataFolder.resolve("library-files");
    private final Path dataFile = dataFolder.resolve("library.dat");

    private FlowPane resourceGrid;
    private FlowPane workGrid;
    private TextField searchField;

    private final String backgroundColor = "#f8f8f6";
    private final String textColor = "#292929";

    public LibraryView() {

        try {
            Files.createDirectories(fileFolder);
        } catch (IOException e) {
            showError("Could not create the Library folder.");
        }

        loadLibrary();
        createView();
    }

    // --------------------------------------------------
    // Main view
    // --------------------------------------------------

    private void createView() {

        Label title = new Label("Library");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + textColor + ";"
        );

        Label description = new Label(
                "Gather knowledge. Preserve what you create."
        );

        description.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #777777;"
        );

        searchField = new TextField();
        searchField.setPromptText("Search your library...");
        searchField.setMaxWidth(350);
        searchField.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 7;" +
                "-fx-border-color: #e5e5e0;" +
                "-fx-border-radius: 7;" +
                "-fx-padding: 10;"
        );

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> refreshLibrary()
        );

        VBox heading = new VBox(8, title, description, searchField);
        heading.setPadding(new Insets(25, 30, 20, 30));

        setTop(heading);

        resourceGrid = new FlowPane();
        resourceGrid.setHgap(18);
        resourceGrid.setVgap(18);

        workGrid = new FlowPane();
        workGrid.setHgap(18);
        workGrid.setVgap(18);

        Tab resourcesTab = new Tab("Resources");
        resourcesTab.setClosable(false);
        resourcesTab.setContent(
                createSection(
                        "Resources",
                        "Articles, books, websites and other material worth keeping.",
                        resourceGrid,
                        true
                )
        );

        Tab workTab = new Tab("My Work");
        workTab.setClosable(false);
        workTab.setContent(
                createSection(
                        "My Work",
                        "Your own notes, projects, documents and creations.",
                        workGrid,
                        false
                )
        );

        TabPane tabs = new TabPane(resourcesTab, workTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
                "-fx-background-color: " + backgroundColor + ";"
        );

        setCenter(tabs);

        refreshLibrary();
    }

    private VBox createSection(
            String heading,
            String description,
            FlowPane grid,
            boolean isResource) {

        Label title = new Label(heading);
        title.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + textColor + ";"
        );

        Label subtitle = new Label(description);
        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #777777;"
        );

        Button addButton = new Button(
                isResource ? "+ Add resource" : "+ Add work"
        );

        styleButton(addButton, true);

        VBox sectionHeader;

        if (isResource) {

            addButton.setOnAction(event -> addResource());
            sectionHeader = new VBox(7, title, subtitle, addButton);

        } else {

            addButton.setOnAction(event -> addWork());

            Button addFolderButton = new Button("+ Add folder");
            styleButton(addFolderButton, false);
            addFolderButton.setOnAction(event -> addFolder());

            HBox workActions = new HBox(8, addButton, addFolderButton);
            workActions.setAlignment(Pos.CENTER_LEFT);

            sectionHeader = new VBox(7, title, subtitle, workActions);
        }

        VBox content = new VBox(22, sectionHeader, grid);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle(
                "-fx-background-color: " + backgroundColor + ";"
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: " + backgroundColor + ";" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
        );

        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return wrapper;
    }

    // --------------------------------------------------
    // Refresh and display
    // --------------------------------------------------

    private void refreshLibrary() {

        if (resourceGrid == null || workGrid == null) {
            return;
        }

        resourceGrid.getChildren().clear();
        workGrid.getChildren().clear();

        String search = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        for (LibraryItem item : items) {

            boolean matches =
                    item.title.toLowerCase(Locale.ROOT).contains(search)
                    || item.description.toLowerCase(Locale.ROOT).contains(search)
                    || item.category.toLowerCase(Locale.ROOT).contains(search);

            if (!matches) {
                continue;
            }

            VBox card = createCard(item);

            if (item.type.equals("resource")) {
                resourceGrid.getChildren().add(card);
            } else {
                workGrid.getChildren().add(card);
            }
        }

        if (resourceGrid.getChildren().isEmpty()) {
            resourceGrid.getChildren().add(
                    createEmptyMessage("No resources yet. Add something worth keeping.")
            );
        }

        if (workGrid.getChildren().isEmpty()) {
            workGrid.getChildren().add(
                    createEmptyMessage("Nothing here yet. Add something you've created.")
            );
        }
    }

    private VBox createCard(LibraryItem item) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(245);
        card.setMinHeight(190);
        card.setMaxWidth(245);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #eaeae6;" +
                "-fx-border-radius: 10;"
        );

        Label category = new Label(item.category.toUpperCase(Locale.ROOT));
        category.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #999999;"
        );

        Label title = new Label(item.title);
        title.setWrapText(true);
        title.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + textColor + ";"
        );

        Label description = new Label(item.description);
        description.setWrapText(true);
        description.setMaxWidth(210);
        description.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #666666;"
        );

        Label date = new Label(item.date);
        date.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #aaaaaa;"
        );

        Button open = new Button("Open");
        styleButton(open, false);
        open.setDisable(item.location.isBlank());

        open.setOnAction(event -> openItem(item));

        Button remove = new Button("Remove");
        styleButton(remove, false);

        remove.setOnAction(event -> {
            items.remove(item);
            saveLibrary();
            refreshLibrary();
        });

        HBox actions = new HBox(8, open, remove);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                category,
                title,
                description,
                date,
                actions
        );

        return card;
    }

    private VBox createEmptyMessage(String message) {

        Label label = new Label(message);
        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #999999;"
        );

        VBox box = new VBox(label);
        box.setPadding(new Insets(15, 0, 15, 0));

        return box;
    }

    // --------------------------------------------------
    // Add resources
    // --------------------------------------------------

    private void addResource() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Add resource");
        dialog.setHeaderText("Save something worth learning from");

        ButtonType saveButton = new ButtonType(
                "Save resource",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField title = new TextField();
        title.setPromptText("Resource title");

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll(
                "Article",
                "Book",
                "Website",
                "Video",
                "Lecture",
                "Other"
        );
        category.setValue("Article");
        category.setMaxWidth(Double.MAX_VALUE);

        TextField link = new TextField();
        link.setPromptText("https://...");

        TextArea description = new TextArea();
        description.setPromptText("What makes this resource interesting?");
        description.setWrapText(true);
        description.setPrefRowCount(3);

        GridPane form = createForm();

        form.add(new Label("Title"), 0, 0);
        form.add(title, 1, 0);

        form.add(new Label("Type"), 0, 1);
        form.add(category, 1, 1);

        form.add(new Label("Link"), 0, 2);
        form.add(link, 1, 2);

        form.add(new Label("Description"), 0, 3);
        form.add(description, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(450);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.setDisable(true);

        title.textProperty().addListener((obs, oldValue, newValue) ->
                save.setDisable(newValue.isBlank())
        );

        dialog.showAndWait().ifPresent(result -> {

            if (result != saveButton) {
                return;
            }

            LibraryItem item = new LibraryItem(
                    "resource",
                    title.getText().trim(),
                    category.getValue(),
                    description.getText().trim(),
                    link.getText().trim(),
                    currentDate()
            );

            items.add(item);
            saveLibrary();
            refreshLibrary();
        });
    }

    // --------------------------------------------------
    // Add own work
    // --------------------------------------------------

    private void addWork() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Add to My Work");
        dialog.setHeaderText("Save something you've created");

        ButtonType saveButton = new ButtonType(
                "Save work",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField title = new TextField();
        title.setPromptText("What did you create?");

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll(
                "Note",
                "Project",
                "Document",
                "Code",
                "Other"
        );
        category.setValue("Project");
        category.setMaxWidth(Double.MAX_VALUE);

        TextArea description = new TextArea();
        description.setPromptText("Describe your work or write your note...");
        description.setWrapText(true);
        description.setPrefRowCount(4);

        TextField location = new TextField();
        location.setPromptText("Optional link or file");

        Button browse = new Button("Browse");
        styleButton(browse, false);

        browse.setOnAction(event -> {

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a file");

            File file = chooser.showOpenDialog(
                    getScene().getWindow()
            );

            if (file != null) {
                location.setText(file.getAbsolutePath());
            }
        });

        HBox fileRow = new HBox(8, location, browse);
        HBox.setHgrow(location, Priority.ALWAYS);

        GridPane form = createForm();

        form.add(new Label("Title"), 0, 0);
        form.add(title, 1, 0);

        form.add(new Label("Type"), 0, 1);
        form.add(category, 1, 1);

        form.add(new Label("Description / note"), 0, 2);
        form.add(description, 1, 2);

        form.add(new Label("File or link"), 0, 3);
        form.add(fileRow, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(500);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.setDisable(true);

        title.textProperty().addListener((obs, oldValue, newValue) ->
                save.setDisable(newValue.isBlank())
        );

        dialog.showAndWait().ifPresent(result -> {

            if (result != saveButton) {
                return;
            }

            String savedLocation = copyWorkFile(
                    location.getText().trim()
            );

            LibraryItem item = new LibraryItem(
                    "work",
                    title.getText().trim(),
                    category.getValue(),
                    description.getText().trim(),
                    savedLocation,
                    currentDate()
            );

            items.add(item);
            saveLibrary();
            refreshLibrary();
        });
    }

    // --------------------------------------------------
    // Add a whole folder to My Work
    // --------------------------------------------------

    private void addFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder to add to My Work");

        File selectedFolder = chooser.showDialog(getScene().getWindow());

        if (selectedFolder == null) {
            return;
        }

        Path source = selectedFolder.toPath();

        String savedLocation = copyWorkFolder(source);

        if (savedLocation.isBlank()) {
            return;
        }

        LibraryItem item = new LibraryItem(
                "work",
                source.getFileName().toString(),
                "Folder",
                "Imported folder and its contents.",
                savedLocation,
                currentDate()
        );

        items.add(item);
        saveLibrary();
        refreshLibrary();
    }

    // --------------------------------------------------
    // File handling
    // --------------------------------------------------

    private String copyWorkFile(String location) {

        if (location.isBlank()) {
            return "";
        }

        if (location.startsWith("http://")
                || location.startsWith("https://")) {
            return location;
        }

        try {

            Path source = Paths.get(location);

            if (!Files.exists(source) || !Files.isRegularFile(source)) {
                return location;
            }

            String filename = System.currentTimeMillis()
                    + "_" + source.getFileName();

            Path destination = fileFolder.resolve(filename);

            Files.copy(source, destination);

            return destination.toString();

        } catch (Exception e) {
            showError("Could not copy the file.");
            return "";
        }
    }

    private String copyWorkFolder(Path source) {

        Path normalizedSource = source.toAbsolutePath().normalize();
        Path normalizedLibraryFolder = fileFolder.toAbsolutePath().normalize();

        if (normalizedSource.startsWith(normalizedLibraryFolder)
                || normalizedLibraryFolder.startsWith(normalizedSource)) {
            showError("Choose a folder outside Eudaimonia's Library storage.");
            return "";
        }

        if (!Files.isDirectory(normalizedSource)) {
            showError("The selected folder could not be found.");
            return "";
        }

        Path destination = fileFolder.resolve(
                UUID.randomUUID() + "_" + normalizedSource.getFileName()
        );

        try (var paths = Files.walk(normalizedSource)) {

            for (Path path : (Iterable<Path>) paths::iterator) {

                Path relativePath = normalizedSource.relativize(path);
                Path target = destination.resolve(relativePath);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }

            return destination.toString();

        } catch (IOException e) {
            deleteFolderQuietly(destination);
            showError("Could not copy the selected folder.");
            return "";
        }
    }

    private void deleteFolderQuietly(Path folder) {

        if (!Files.exists(folder)) {
            return;
        }

        try (var paths = Files.walk(folder)) {

            paths.sorted((first, second) -> second.compareTo(first))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup after an incomplete copy.
                        }
                    });

        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
    }

    // --------------------------------------------------
    // Open files, folders and links
    // --------------------------------------------------

    private void openItem(LibraryItem item) {

        try {

            String location = item.location;

            if (location.startsWith("http://")
                    || location.startsWith("https://")) {

                new ProcessBuilder("xdg-open", location).start();
                return;
            }

            Path path = Paths.get(location);

            if (Files.exists(path)) {

                new ProcessBuilder("xdg-open", path.toString()).start();

            } else {

                showError("The saved file or folder could not be found.");
            }

        } catch (IOException | InvalidPathException e) {
            showError("Could not open this resource.");
        }
    }

    // --------------------------------------------------
    // Persistence
    // --------------------------------------------------

    private void saveLibrary() {

        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(dataFile))) {

            out.writeObject(items);

        } catch (IOException e) {
            showError("Could not save the Library.");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadLibrary() {

        if (!Files.exists(dataFile)) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(dataFile))) {

            items.addAll((List<LibraryItem>) in.readObject());

        } catch (IOException | ClassNotFoundException e) {
            showError("Could not load the Library.");
        }
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    private GridPane createForm() {

        GridPane form = new GridPane();

        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(15));

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(100);

        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(
                labelColumn,
                inputColumn
        );

        return form;
    }

    private String currentDate() {

        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
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
                    "-fx-padding: 7 12 7 12;" +
                    "-fx-cursor: hand;"
            );
        }
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eudaimonia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --------------------------------------------------
    // Library item model
    // --------------------------------------------------

    private static class LibraryItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;
        private final String title;
        private final String category;
        private final String description;
        private final String location;
        private final String date;

        private LibraryItem(
                String type,
                String title,
                String category,
                String description,
                String location,
                String date) {

            this.type = type;
            this.title = title;
            this.category = category;
            this.description = description;
            this.location = location;
            this.date = date;
        }
    }
}
