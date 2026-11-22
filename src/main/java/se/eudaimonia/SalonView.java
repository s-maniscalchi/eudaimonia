package se.eudaimonia;

import se.eudaimonia.ai.OllamaClient;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalonView extends BorderPane {

    private final OllamaClient ollamaClient = new OllamaClient();

    private final List<Map<String, String>> chatHistory =
            new ArrayList<>();

    private TextArea conversation;
    private TextArea thoughts;
    private TextField messageInput;
    private Button sendButton;
    private Button conclusionButton;

    private static final String SYSTEM_PROMPT = """
            You are a thoughtful Socratic discussion partner.

            Your purpose is to help the user think deeply and develop
            their own ideas.

            Follow these principles:

            1. Ask thoughtful follow-up questions.
            2. Ask one question at a time.
            3. Challenge assumptions respectfully.
            4. Do not immediately give away complete answers.
            5. Help the user explore different perspectives.
            6. Be intellectually honest and concise.
            7. Adapt to the language used by the user.

            You are not a generic assistant. You are a partner
            in careful thinking.
            """;

    public SalonView() {

        chatHistory.add(Map.of(
                "role", "system",
                "content", SYSTEM_PROMPT
        ));

        createView();
    }

    private void createView() {

        setPadding(new Insets(5, 0, 10, 0));

        // Discussion area

        Label discussionTitle = new Label("Discussion");

        discussionTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #292929;"
        );

        conversation = new TextArea();
        conversation.setEditable(false);
        conversation.setWrapText(true);
        conversation.setPromptText(
                "Your conversation with your thinking partner will appear here."
        );

        conversation.setStyle(
                "-fx-control-inner-background: white;" +
                "-fx-background-color: white;" +
                "-fx-border-color: #eaeae6;" +
                "-fx-font-size: 13px;"
        );

        VBox.setVgrow(conversation, Priority.ALWAYS);

        messageInput = new TextField();
        messageInput.setPromptText("Write your thoughts...");

        sendButton = new Button("Send");
        styleButton(sendButton, true);

        sendButton.setOnAction(event -> sendMessage());

        messageInput.setOnAction(event -> sendMessage());

        HBox inputArea = new HBox(10, messageInput, sendButton);
        inputArea.setAlignment(Pos.CENTER);

        HBox.setHgrow(messageInput, Priority.ALWAYS);

        VBox discussionPanel = new VBox(
                15,
                discussionTitle,
                conversation,
                inputArea
        );

        discussionPanel.setPadding(new Insets(15));
        discussionPanel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #eaeae6;" +
                "-fx-border-radius: 10;"
        );

        HBox.setHgrow(discussionPanel, Priority.ALWAYS);

        // Personal thoughts area

        Label thoughtsTitle = new Label("Your thoughts");

        thoughtsTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #292929;"
        );

        thoughts = new TextArea();
        thoughts.setWrapText(true);
        thoughts.setPromptText(
                "Write down your own thoughts and reflections..."
        );

        thoughts.setStyle(
                "-fx-control-inner-background: white;" +
                "-fx-background-color: white;" +
                "-fx-border-color: #eaeae6;" +
                "-fx-font-size: 13px;"
        );

        VBox.setVgrow(thoughts, Priority.ALWAYS);

        conclusionButton = new Button("Conclude session");
        styleButton(conclusionButton, false);

        conclusionButton.setOnAction(event -> concludeSession());

        VBox thoughtsPanel = new VBox(
                15,
                thoughtsTitle,
                thoughts,
                conclusionButton
        );

        thoughtsPanel.setPadding(new Insets(15));
        thoughtsPanel.setPrefWidth(280);
        thoughtsPanel.setMinWidth(230);
        thoughtsPanel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #eaeae6;" +
                "-fx-border-radius: 10;"
        );

        HBox content = new HBox(
                20,
                discussionPanel,
                thoughtsPanel
        );

        content.setAlignment(Pos.TOP_LEFT);

        setCenter(content);
    }

    private void sendMessage() {

        String message = messageInput.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        messageInput.clear();

        chatHistory.add(Map.of(
                "role", "user",
                "content", message
        ));

        conversation.appendText(
                "You:\n" + message + "\n\n"
        );

        sendButton.setDisable(true);
        messageInput.setDisable(true);

        ollamaClient.chat(List.copyOf(chatHistory))
                .whenComplete((answer, error) ->

                    Platform.runLater(() -> {

                        if (error != null) {

                            conversation.appendText(
                                    "Could not reach Ollama. "
                                    + "Make sure it is running.\n\n"
                            );

                        } else {

                            chatHistory.add(Map.of(
                                    "role", "assistant",
                                    "content", answer
                            ));

                            conversation.appendText(
                                    "Thinking partner:\n"
                                    + answer + "\n\n"
                            );
                        }

                        sendButton.setDisable(false);
                        messageInput.setDisable(false);
                        messageInput.requestFocus();
                    })
                );
    }

    private void concludeSession() {

        if (chatHistory.size() <= 1) {

            showInformation(
                    "There is no discussion to summarize yet."
            );

            return;
        }

        conclusionButton.setDisable(true);

        List<Map<String, String>> summaryHistory =
                new ArrayList<>(chatHistory);

        String prompt = """
                Please summarize our discussion.

                Include:
                - Main question or topic
                - Key insights
                - Important assumptions or perspectives explored
                - Open questions
                - Possible next step for the Library room

                Also consider these personal notes:

                %s

                Keep the summary clear and useful.
                """.formatted(thoughts.getText());

        summaryHistory.add(Map.of(
                "role", "user",
                "content", prompt
        ));

        conclusionButton.setText("Thinking...");

        ollamaClient.chat(summaryHistory)
                .whenComplete((answer, error) ->

                    Platform.runLater(() -> {

                        conclusionButton.setDisable(false);
                        conclusionButton.setText("Conclude session");

                        if (error != null) {

                            showInformation(
                                    "Could not generate the summary. "
                                    + "Make sure Ollama is running."
                            );

                        } else {

                            showSummary(answer);
                        }
                    })
                );
    }

    private void showSummary(String summary) {

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);

        dialog.setTitle("Session conclusion");
        dialog.setHeaderText("Your discussion summary");

        TextArea summaryArea = new TextArea(summary);
        summaryArea.setEditable(false);
        summaryArea.setWrapText(true);
        summaryArea.setPrefRowCount(15);
        summaryArea.setPrefColumnCount(55);

        dialog.getDialogPane().setContent(summaryArea);

        dialog.showAndWait();
    }

    private void showInformation(String message) {

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);

        dialog.setTitle("Salon");
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        dialog.showAndWait();
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
                    "-fx-padding: 10 18 10 18;" +
                    "-fx-cursor: hand;"
            );
        }
    }
}
