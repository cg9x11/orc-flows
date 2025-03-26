package asia.noe.flows.nodes.impl;

import com.rabbitmq.client.*;

import asia.noe.flows.CredentialManager;
import asia.noe.flows.NodeType;
import asia.noe.flows.RabbitMQCredential;
import asia.noe.flows.RabbitMQResponseDispatcher;
import asia.noe.flows.nodes.BaseNode;
import asia.noe.flows.nodes.WorkflowNode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Node tích hợp RabbitMQ:
 * - Gửi dữ liệu vào inputQueue (dùng để gọi service bên ngoài).
 * - Đăng ký response để nhận phản hồi từ outputQueue chung qua RabbitMQResponseDispatcher.
 */
public class RabbitMQServiceNode extends BaseNode {
    private final String requestQueue;
    private final String responseQueue;
    private static Connection connection;
    private static Channel channel;

    static {
        try {
            RabbitMQCredential credential = CredentialManager.getRabbitMQCredential();
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(credential.getHost());
            factory.setPort(credential.getPort());
            factory.setUsername(credential.getUsername());
            factory.setPassword(credential.getPassword());
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            throw new RuntimeException("Kết nối RabbitMQ thất bại", e);
        }
    }

    public RabbitMQServiceNode(String id, String name, String requestQueue, String responseQueue, List<WorkflowNode> nextNodes) {
        super(id, name, NodeType.TASK, nextNodes);
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
    }

    @Override
    public CompletableFuture<Map<String, Object>> execute(String sessionId, Map<String, Object> inputData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();
                RabbitMQResponseDispatcher.registerResponse(sessionId, this.id, responseFuture);
                String message = "{\"sessionId\": \"" + sessionId + "\", \"nodeId\": \"" + this.id + "\", \"responseQueue\": \"" + this.responseQueue + "\", \"data\": " + inputData + "}";
                channel.basicPublish("", requestQueue, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(System.currentTimeMillis() + " Request: " + message);
                return responseFuture.thenApply(response -> {
                    System.out.println(System.currentTimeMillis() + " Response: " + response);
                    return response;
                }).join();
            } catch (Exception e) {
                throw new RuntimeException("Xử lý RabbitMQServiceNode thất bại", e);
            }
        });
    }
}