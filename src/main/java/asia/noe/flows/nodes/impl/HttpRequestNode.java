package asia.noe.flows.nodes.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import asia.noe.flows.NodeType;
import asia.noe.flows.nodes.BaseNode;
import asia.noe.flows.nodes.WorkflowNode;

/**
 * Node gửi HTTP request đến API và chờ phản hồi.
 */
public class HttpRequestNode extends BaseNode {
    private final String apiUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpRequestNode(String id, String name, String apiUrl, List<WorkflowNode> nextNodes) {
        super(id, name, NodeType.TASK, nextNodes);
        this.apiUrl = apiUrl;
    }

    @Override
    public CompletableFuture<Map<String, Object>> execute(String sessionId, Map<String, Object> inputData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = "{\"sessionId\": \"" + sessionId + "\", \"data\": " + inputData + "}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Map<String, Object> outputData = new HashMap<>(inputData);
                outputData.put("apiResponse", response.body());
                return outputData;
            } catch (Exception e) {
                throw new RuntimeException("HTTP Request Failed", e);
            }
        });
    }
}