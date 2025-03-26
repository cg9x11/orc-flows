package asia.noe.flows.nodes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import asia.noe.flows.NodeType;

/**
 * Interface chung cho các node trong workflow.
 */
public interface WorkflowNode {
    String getId();
    String getName();
    NodeType getType();
    List<WorkflowNode> getNextNodes();
    void addIncomingNode(WorkflowNode node);
    /**
     * Thực thi node với sessionId và dữ liệu đầu vào.
     */
    CompletableFuture<Map<String, Object>> execute(String sessionId, Map<String, Object> inputData);
}
