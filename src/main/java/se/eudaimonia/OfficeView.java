package se.eudaimonia;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OfficeView extends BorderPane {

    private static final Path DATA_FOLDER =
            Paths.get(System.getProperty("user.home"), ".eudaimonia");

    private static final Path DATA_FILE =
            DATA_FOLDER.resolve("office.dat");

    // Colors
    private static final String BACKGROUND = "#F5F5F0";
    private static final String CARD = "#FFFFFF";
    private static final String TEXT = "#252525";
    private static final String SECONDARY = "#777777";
    private static final String ACCENT = "#82947A";
    private static final String BORDER = "#E5E5DF";

    private final List<Project> projects = new ArrayList<>();

    private final ListView<Project> projectList = new ListView<>();
    private final ListView<String> taskList = new ListView<>();

    private final ComboBox<Project> sessionProject = new ComboBox<>();

    private final TextField sessionTitle = new TextField();
    private final TextField sessionDuration = new TextField();

    private final TextArea sessionObjective = new TextArea();
    private final TextArea sessionTasks = new TextArea();
    private final TextArea sessionResources = new TextArea();
    private final TextArea sessionFiles = new TextArea();
    private final TextArea sessionTools = new TextArea();
    private final TextArea sessionEnvironment = new TextArea();

    private Label statusLabel;

    private SessionPreparation preparation = new SessionPreparation();

    private Button projectsButton;
    private Button preparationButton;

    private StackPane contentArea;

    public OfficeView() {
	getStyleClass().add("room-root");
        loadData();
        createView();
    }

    // --------------------------------------------------
    // Main view
    // --------------------------------------------------

    private void createView() {

        setStyle("-fx-background-color: " + BACKGROUND + ";");

        Label title = new Label("Office");
        title.setFont(Font.font("System", 30));
        title.setStyle("-fx-text-fill: " + TEXT + ";");

        Label subtitle = new Label(
                "Organize your work and prepare for execution."
        );
        subtitle.setStyle(
                "-fx-text-fill: " + SECONDARY + "; -fx-font-size: 13px;"
        );

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(30, 30, 20, 30));
        header.setMaxWidth(1050);

        StackPane headerWrapper = new StackPane(header);
        headerWrapper.setAlignment(Pos.CENTER);

        projectsButton = createNavigationButton("Projects");
        preparationButton = createNavigationButton("Session Preparation");

        projectsButton.setOnAction(e -> showProjects());
        preparationButton.setOnAction(e -> showPreparation());

        HBox navigation = new HBox(
                10,
                projectsButton,
                preparationButton
        );
        navigation.setAlignment(Pos.CENTER_LEFT);
        navigation.setPadding(new Insets(0, 0, 20, 0));

        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_CENTER);

        VBox workspace = new VBox(0, navigation, contentArea);
        workspace.setMaxWidth(1050);
        workspace.setPadding(new Insets(0, 30, 30, 30));

        StackPane workspaceWrapper = new StackPane(workspace);
        workspaceWrapper.setAlignment(Pos.TOP_CENTER);

        setTop(headerWrapper);
        setCenter(workspaceWrapper);

        refreshProjects();
        showProjects();
    }

    // --------------------------------------------------
    // Navigation
    // --------------------------------------------------

    private Button createNavigationButton(String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + SECONDARY + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 7;"
        );

        return button;
    }

    private void showProjects() {

        projectsButton.setStyle(
                "-fx-background-color: " + ACCENT + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 7;"
        );

        preparationButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + SECONDARY + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 7;"
        );

        contentArea.getChildren().setAll(createProjectsView());
    }

    private void showPreparation() {

        preparationButton.setStyle(
                "-fx-background-color: " + ACCENT + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 7;"
        );

        projectsButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + SECONDARY + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 7;"
        );

        contentArea.getChildren().setAll(createPreparationView());
    }

    // --------------------------------------------------
    // Projects
    // --------------------------------------------------

    private Node createProjectsView() {

        Label heading = createHeading("Your projects");

        Label description = createDescription(
                "Keep track of the larger things you are working towards."
        );

        Button addProjectButton = createPrimaryButton("+ New project");
        addProjectButton.setOnAction(e -> addProject());

        Button removeProjectButton = createSecondaryButton("Remove project");
        removeProjectButton.setOnAction(e -> removeProject());

        HBox buttons = new HBox(
                10,
                addProjectButton,
                removeProjectButton
        );

        projectList.setPlaceholder(new Label("No projects yet."));
        projectList.setPrefHeight(350);

        projectList.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        projectList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Project project, boolean empty) {

                super.updateItem(project, empty);

                if (empty || project == null) {
                    setText(null);
                    setStyle("-fx-background-color: white;");
                } else {

                    setText(
                            project.name + "\n" +
                            project.status + "  ·  " +
                            project.tasks.size() + " tasks"
                    );

                    setStyle(
                            "-fx-padding: 12;" +
                            "-fx-background-color: white;" +
                            "-fx-text-fill: " + TEXT + ";"
                    );
                }
            }
        });

        Label taskHeading = createHeading("Project tasks");

        taskList.setPlaceholder(
                new Label("Select a project to see its tasks.")
        );

        taskList.setPrefHeight(270);

        taskList.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        taskList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(String task, boolean empty) {

                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                } else {
                    setText(task);
                    setStyle(
                            "-fx-padding: 10;" +
                            "-fx-text-fill: " + TEXT + ";"
                    );
                }
            }
        });

        Button addTaskButton = createPrimaryButton("+ Add task");
        addTaskButton.setOnAction(e -> addTask());

        Button completeTaskButton = createSecondaryButton("Complete task");
        completeTaskButton.setOnAction(e -> completeTask());

        Button removeTaskButton = createSecondaryButton("Remove task");
        removeTaskButton.setOnAction(e -> removeTask());

        HBox taskButtons = new HBox(
                8,
                addTaskButton,
                completeTaskButton,
                removeTaskButton
        );

        VBox projectCard = createCard(
                new Label("PROJECTS"),
                projectList,
                buttons
        );

        VBox taskCard = createCard(
                taskHeading,
                taskList,
                taskButtons
        );

        HBox columns = new HBox(18, projectCard, taskCard);

        HBox.setHgrow(projectCard, Priority.ALWAYS);
        HBox.setHgrow(taskCard, Priority.ALWAYS);

        projectCard.setPrefWidth(420);
        taskCard.setPrefWidth(580);

        projectList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> refreshTasks());

        VBox page = new VBox(
                16,
                heading,
                description,
                columns
        );

        page.setMaxWidth(990);

        return page;
    }

    private void addProject() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("New project");
        dialog.setHeaderText("Create a project");
        dialog.setContentText("Project name:");

        dialog.showAndWait().ifPresent(name -> {

            if (name.isBlank()) {
                return;
            }

            Project project = new Project(name.trim());

            projects.add(project);

            saveData();
            refreshProjects();

            projectList.getSelectionModel().select(project);
        });
    }

    private void removeProject() {

        Project selected =
                projectList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Select a project first.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Remove project");
        confirmation.setHeaderText("Remove " + selected.name + "?");
        confirmation.setContentText(
                "The project and its tasks will be removed."
        );

        confirmation.showAndWait().ifPresent(result -> {

            if (result == ButtonType.OK) {

                projects.remove(selected);

                saveData();
                refreshProjects();
                refreshTasks();
            }
        });
    }

    private void addTask() {

        Project selected =
                projectList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Select a project first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("New task");
        dialog.setHeaderText("Add a task to " + selected.name);
        dialog.setContentText("Task:");

        dialog.showAndWait().ifPresent(task -> {

            if (task.isBlank()) {
                return;
            }

            selected.tasks.add(new Task(task.trim()));

            saveData();
            refreshTasks();
            refreshProjects();
            projectList.getSelectionModel().select(selected);
        });
    }

    private void completeTask() {

        Project selected =
                projectList.getSelectionModel().getSelectedItem();

        int index = taskList.getSelectionModel().getSelectedIndex();

        if (selected == null || index < 0) {
            showMessage("Select a task first.");
            return;
        }

        selected.tasks.get(index).completed = true;

        saveData();
        refreshTasks();
        refreshProjects();
        projectList.getSelectionModel().select(selected);
    }

    private void removeTask() {

        Project selected =
                projectList.getSelectionModel().getSelectedItem();

        int index = taskList.getSelectionModel().getSelectedIndex();

        if (selected == null || index < 0) {
            showMessage("Select a task first.");
            return;
        }

        selected.tasks.remove(index);

        saveData();
        refreshTasks();
        refreshProjects();
        projectList.getSelectionModel().select(selected);
    }

    private void refreshProjects() {

        Project selected = projectList.getSelectionModel().getSelectedItem();

        projectList.setItems(
                FXCollections.observableArrayList(projects)
        );

        sessionProject.setItems(
                FXCollections.observableArrayList(projects)
        );

        if (selected != null && projects.contains(selected)) {
            projectList.getSelectionModel().select(selected);
        }
    }

    private void refreshTasks() {

        Project selected =
                projectList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            taskList.getItems().clear();
            return;
        }

        List<String> displayedTasks = new ArrayList<>();

        for (Task task : selected.tasks) {

            displayedTasks.add(
                    (task.completed ? "✓   " : "○   ") +
                    task.description
            );
        }

        taskList.setItems(
                FXCollections.observableArrayList(displayedTasks)
        );
    }

    // --------------------------------------------------
    // Session preparation
    // --------------------------------------------------

    private Node createPreparationView() {

        Label heading = createHeading("Prepare your next session");

        Label description = createDescription(
                "Everything should be ready before you enter Deep Work."
        );

        sessionTitle.setPromptText("E.g. Database assignment");

        sessionProject.setPromptText("Choose a project");
        sessionProject.setMaxWidth(Double.MAX_VALUE);

        sessionDuration.setPromptText("E.g. 60 minutes");

        configureTextArea(
                sessionObjective,
                "What exactly do you want to accomplish?",
                3
        );

        configureTextArea(
                sessionTasks,
                "Write the steps you will follow, one per line.",
                4
        );

        configureTextArea(
                sessionResources,
                "Add links to articles, lectures, documentation or other resources.",
                3
        );

        configureTextArea(
                sessionFiles,
                "Add paths to files and folders you need.",
                3
        );

        configureTextArea(
                sessionTools,
                "Which programs, tools or equipment will you need?",
                3
        );

        configureTextArea(
                sessionEnvironment,
                "What needs to be prepared before you start?",
                3
        );

        VBox sessionCard = createCard(
                createSectionTitle("01  ·  Session"),
                createField("Session name", sessionTitle),
                createField("Project", sessionProject),
                createField("Planned duration", sessionDuration)
        );

        VBox objectiveCard = createCard(
                createSectionTitle("02  ·  Objective"),
                createDescription(
                        "Define what a successful work session looks like."
                ),
                createField("Your objective", sessionObjective)
        );

        VBox tasksCard = createCard(
                createSectionTitle("03  ·  Tasks"),
                createDescription(
                        "Break the work into clear, actionable steps."
                ),
                createField("Work steps", sessionTasks)
        );

        VBox resourcesCard = createCard(
                createSectionTitle("04  ·  Resources"),
                createDescription(
                        "Collect the knowledge and references you will need."
                ),
                createField("Links and references", sessionResources)
        );

        VBox filesCard = createCard(
                createSectionTitle("05  ·  Files"),
                createDescription(
                        "Prepare the documents, folders and files you will use."
                ),
                createField("Files and documents", sessionFiles)
        );

        VBox toolsCard = createCard(
                createSectionTitle("06  ·  Tools & Environment"),
                createField("Tools and programs", sessionTools),
                createField("Environment preparation", sessionEnvironment)
        );

        Button saveButton = createSecondaryButton("Save preparation");
        saveButton.setOnAction(e -> savePreparation());

        Button readyButton = createPrimaryButton("Prepare for Deep Work  →");
        readyButton.setOnAction(e -> checkPreparation());

        statusLabel = new Label(
                "Your preparation will be saved locally."
        );

        statusLabel.setStyle(
                "-fx-text-fill: " + SECONDARY + ";" +
                "-fx-font-size: 12px;"
        );

        HBox actions = new HBox(
                12,
                saveButton,
                readyButton
        );

        actions.setAlignment(Pos.CENTER_LEFT);

        VBox finishCard = createCard(
                createSectionTitle("Ready to begin?"),
                createDescription(
                        "Make sure everything you need is prepared before entering Deep Work."
                ),
                actions,
                statusLabel
        );

        VBox page = new VBox(
                16,
                heading,
                description,
                sessionCard,
                objectiveCard,
                tasksCard,
                resourcesCard,
                filesCard,
                toolsCard,
                finishCard
        );

        page.setMaxWidth(900);
        page.setPadding(new Insets(0, 0, 25, 0));

        ScrollPane scrollPane = new ScrollPane(page);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
        );

        return scrollPane;
    }

    private void configureTextArea(
            TextArea area,
            String prompt,
            int rows
    ) {

        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        area.setStyle(
                "-fx-control-inner-background: #FAFAF8;" +
                "-fx-background-color: #FAFAF8;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-prompt-text-fill: #A0A09A;" +
                "-fx-font-size: 13px;"
        );
    }

    private VBox createField(String label, Node field) {

        Label heading = new Label(label);

        heading.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );

        VBox box = new VBox(7, heading, field);

        return box;
    }

    // --------------------------------------------------
    // Save and load preparation
    // --------------------------------------------------

    private void savePreparation() {

        preparation.title = sessionTitle.getText();

        preparation.projectName =
                sessionProject.getValue() == null
                        ? ""
                        : sessionProject.getValue().name;

        preparation.duration = sessionDuration.getText();
        preparation.objective = sessionObjective.getText();
        preparation.tasks = sessionTasks.getText();
        preparation.resources = sessionResources.getText();
        preparation.files = sessionFiles.getText();
        preparation.tools = sessionTools.getText();
        preparation.environment = sessionEnvironment.getText();

        preparation.lastUpdated = LocalDateTime.now().toString();

        saveData();

        statusLabel.setText("Preparation saved locally.");
    }

    private void loadPreparationIntoFields() {

        sessionTitle.setText(preparation.title);
        sessionDuration.setText(preparation.duration);
        sessionObjective.setText(preparation.objective);
        sessionTasks.setText(preparation.tasks);
        sessionResources.setText(preparation.resources);
        sessionFiles.setText(preparation.files);
        sessionTools.setText(preparation.tools);
        sessionEnvironment.setText(preparation.environment);

        for (Project project : projects) {

            if (project.name.equals(preparation.projectName)) {
                sessionProject.setValue(project);
                break;
            }
        }
    }

    private void checkPreparation() {

        savePreparation();

        List<String> missing = new ArrayList<>();

        if (preparation.title.isBlank()) {
            missing.add("Session name");
        }

        if (preparation.objective.isBlank()) {
            missing.add("Objective");
        }

        if (preparation.tasks.isBlank()) {
            missing.add("Tasks");
        }

        if (preparation.resources.isBlank()
                && preparation.files.isBlank()
                && preparation.tools.isBlank()) {

            missing.add("Resources, files or tools");
        }

        if (!missing.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle("Preparation incomplete");
            alert.setHeaderText("A few things are still missing.");

            alert.setContentText(
                    String.join("\n", missing)
            );

            alert.showAndWait();

            statusLabel.setText(
                    "More preparation is needed."
            );

            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Ready for Deep Work");
        alert.setHeaderText("Your session is prepared.");

        alert.setContentText(
                "Session: " + preparation.title +
                "\n\nObjective:\n" + preparation.objective +
                "\n\nYou are ready to enter Deep Work."
        );

        alert.showAndWait();

        statusLabel.setText(
                "Session prepared successfully."
        );
    }

    // --------------------------------------------------
    // Design helpers
    // --------------------------------------------------

    private Label createHeading(String text) {

        Label label = new Label(text);

        label.setFont(Font.font("System", 23));
        label.setStyle("-fx-text-fill: " + TEXT + ";");

        return label;
    }

    private Label createSectionTitle(String text) {

        Label label = new Label(text);

        label.setFont(Font.font("System", 15));
        label.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createDescription(String text) {

        Label label = new Label(text);

        label.setWrapText(true);
        label.setStyle(
                "-fx-text-fill: " + SECONDARY + ";" +
                "-fx-font-size: 12px;"
        );

        return label;
    }

    private VBox createCard(Node... children) {

        VBox card = new VBox(14, children);

        card.setPadding(new Insets(22));
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;"
        );

        return card;
    }

    private Button createPrimaryButton(String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: " + ACCENT + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 11 17 11 17;" +
                "-fx-background-radius: 7;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private Button createSecondaryButton(String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: #F2F2ED;" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 11 15 11 15;" +
                "-fx-background-radius: 7;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 7;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    // --------------------------------------------------
    // Local storage
    // --------------------------------------------------

    private void saveData() {

        try {

            Files.createDirectories(DATA_FOLDER);

            try (ObjectOutputStream output =
                         new ObjectOutputStream(
                                 Files.newOutputStream(DATA_FILE)
                         )) {

                output.writeObject(projects);
                output.writeObject(preparation);
            }

        } catch (IOException e) {

            showMessage(
                    "Could not save Office data: " + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {

        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             Files.newInputStream(DATA_FILE)
                     )) {

            projects.addAll((List<Project>) input.readObject());

            preparation = (SessionPreparation) input.readObject();

        } catch (IOException | ClassNotFoundException |
                 ClassCastException e) {

            showMessage(
                    "Could not load Office data: " + e.getMessage()
            );
        }
    }

    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Office");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // --------------------------------------------------
    // Data models
    // --------------------------------------------------

    private static class Project implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private String status = "In progress";
        private List<Task> tasks = new ArrayList<>();

        private Project(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Task implements Serializable {

        private static final long serialVersionUID = 1L;

        private String description;
        private boolean completed;

        private Task(String description) {
            this.description = description;
        }
    }

    private static class SessionPreparation implements Serializable {

        private static final long serialVersionUID = 1L;

        private String title = "";
        private String projectName = "";
        private String duration = "";
        private String objective = "";
        private String tasks = "";
        private String resources = "";
        private String files = "";
        private String tools = "";
        private String environment = "";
        private String lastUpdated = "";
    }
}
