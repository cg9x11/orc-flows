package asia.noe.flows;

import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitMQResponseDispatcher {
    private static final String OUTPUT_QUEUE = "order_out";
    private static Channel channel;
    private static Connection connection;
    // Map chứa các future: key = "sessionId:nodeId"
    private static final ConcurrentHashMap<String, CompletableFuture<Map<String, Object>>> responseMap = new ConcurrentHashMap<>();

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
            channel.queueDeclare(OUTPUT_QUEUE, true, false, false, null);
            channel.basicConsume(OUTPUT_QUEUE, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body, StandardCharsets.UTF_8);
                    try {
                        String sessionId = JsonPath.read(message, "$.sessionId");
                        String nodeId = JsonPath.read(message, "$.nodeId");
                        String key = sessionId + ":" + nodeId;
                        // Parse toàn bộ JSON thành Map (có thể sử dụng thư viện Jackson nếu cần)
                        Map<String, Object> outputData = JsonPath.parse(message).json();
                        CompletableFuture<Map<String, Object>> future = responseMap.remove(key);
                        if (future != null) {
                            future.complete(outputData);
                        }
                    } catch (Exception e) {
                        System.err.println("Error xử lý message: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Khởi tạo RabbitMQResponseDispatcher thất bại", e);
        }
    }
    
    public static void registerResponse(String sessionId, String nodeId, CompletableFuture<Map<String, Object>> future) {
        String key = sessionId + ":" + nodeId;
        responseMap.put(key, future);
    }
}