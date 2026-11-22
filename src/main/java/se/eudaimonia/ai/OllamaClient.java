package se.eudaimonia.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OllamaClient {

    private static final String MODEL = "qwen2.5:3b";

    private static final String API_URL =
            "http://localhost:11434/api/chat";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<String> chat(
            List<Map<String, String>> messages) {

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            requestBody.put("model", MODEL);
            requestBody.put("stream", false);

            ArrayNode messageArray = requestBody.putArray("messages");

            for (Map<String, String> message : messages) {
                ObjectNode messageNode = messageArray.addObject();

                messageNode.put("role", message.get("role"));
                messageNode.put("content", message.get("content"));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody)))
                    .build();

            return httpClient.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            ).thenApply(response -> {

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "Ollama returned status: "
                                    + response.statusCode());
                }

                try {
                    JsonNode json = objectMapper.readTree(response.body());

                    return json.path("message")
                            .path("content")
                            .asText();

                } catch (Exception e) {
                    throw new RuntimeException(
                            "Could not read Ollama response.", e);
                }
            });

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
