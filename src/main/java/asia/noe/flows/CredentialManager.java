package asia.noe.flows;

public class CredentialManager {
    // Ở đây, ví dụ đơn giản trả về credential mặc định dựa trên id.
    public static RabbitMQCredential getRabbitMQCredential() {
        // Trong thực tế, sẽ lấy thông tin từ cơ sở dữ liệu hoặc cấu hình đã được quản lý qua UI.
        return new RabbitMQCredential("localhost", 5672, "guest", "guest");
    }
}