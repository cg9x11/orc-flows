package asia.noe.flows.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeConfig {
    private String id;
    private String name;
    private String type;
    private Map<String, Object> parameters;

    // Connections được định nghĩa riêng trong file JSON dưới dạng: key là channel (ví dụ: "main") và value là danh sách id node đích.
    private Map<String, List<String>> connections;
    private Map<String, Map<String, String>> credentials;
}