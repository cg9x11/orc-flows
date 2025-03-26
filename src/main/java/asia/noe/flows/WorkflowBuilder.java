package asia.noe.flows;

import com.fasterxml.jackson.databind.ObjectMapper;

import asia.noe.flows.models.NodeConfig;
import asia.noe.flows.models.WorkflowConfig;
import asia.noe.flows.nodes.WorkflowNode;
import asia.noe.flows.nodes.impl.HttpRequestNode;
import asia.noe.flows.nodes.impl.MergeNode;
import asia.noe.flows.nodes.impl.RabbitMQServiceNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class WorkflowBuilder {

    /**
     * Xây dựng workflow từ file JSON cấu hình.
     * Nếu không có startNode, tìm node không có incoming connections làm node khởi
     * đầu.
     */
    public static WorkflowNode buildWorkflow(String configFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WorkflowConfig config = mapper.readValue(new File(configFilePath), WorkflowConfig.class);
        Map<String, WorkflowNode> nodeMap = new HashMap<>();

        // Tạo instance các node từ NodeConfig
        for (NodeConfig nc : config.getNodes()) {
            WorkflowNode node = createNodeInstance(nc);
            nodeMap.put(nc.getId(), node);
        }

        // Thiết lập kết nối giữa các node dựa vào connections (tách riêng)
        for (Map.Entry<String, Map<String, List<List<Map<String, String>>>>> entry : config.getConnections().entrySet()) {
            String sourceNodeId = entry.getKey();
            WorkflowNode sourceNode = nodeMap.get(sourceNodeId);

            if (sourceNode != null) {
                Map<String, List<List<Map<String, String>>>> channelConnections = entry.getValue();

                // Duyệt qua các kênh (ví dụ: "main")
                for (List<List<Map<String, String>>> connectionsList : channelConnections.values()) {
                    for (List<Map<String, String>> connectionGroup : connectionsList) {
                        for (Map<String, String> connection : connectionGroup) {
                            String targetNodeId = connection.get("node");
                            WorkflowNode targetNode = nodeMap.get(targetNodeId);

                            if (targetNode != null) {
                                sourceNode.getNextNodes().add(targetNode);
                                targetNode.addIncomingNode(sourceNode);
                            }
                        }
                    }
                }
            }
        }

        // Nếu startNode được định nghĩa, sử dụng nó
        if (config.getStartNode() != null && nodeMap.containsKey(config.getStartNode())) {
            return nodeMap.get(config.getStartNode());
        }

        throw new RuntimeException("Không tìm thấy node khởi đầu.");
    }

    /**
     * Tạo một node instance từ NodeConfig
     */
    private static WorkflowNode createNodeInstance(NodeConfig nc) {
        Map<String, Object> params = nc.getParameters();
        switch (nc.getType()) {
            case "RabbitMQServiceNode":
                return new RabbitMQServiceNode(nc.getId(), nc.getName(),
                        (String) params.get("requestQueue"), (String) params.get("responseQueue"), new ArrayList<>());
            case "MergeNode":
                Integer expected = (Integer) params.get("expectedInputs");
                return new MergeNode(nc.getId(), nc.getName(), expected != null ? expected : 1, new ArrayList<>());
            case "HttpRequestNode":
                return new HttpRequestNode(nc.getId(), nc.getName(),
                        (String) params.get("apiUrl"), new ArrayList<>());
            default:
                throw new IllegalArgumentException("Unsupported node type: " + nc.getType());
        }
    }
}