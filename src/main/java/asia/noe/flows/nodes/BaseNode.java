package asia.noe.flows.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import asia.noe.flows.NodeType;

/**
 * Lớp trừu tượng cho các node, cung cấp các thuộc tính cơ bản.
 */
public abstract class BaseNode implements WorkflowNode {
    protected final String id;
    protected final String name;
    protected final NodeType type;
    protected final List<WorkflowNode> nextNodes;
    // Danh sách các node gửi dữ liệu vào (dành cho các node hợp nhất)
    protected final List<WorkflowNode> incomingNodes = new ArrayList<>();

    public BaseNode(String id, String name, NodeType type, List<WorkflowNode> nextNodes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.nextNodes = nextNodes;
    }

    @Override
    public String getId() { return id; }
    @Override
    public String getName() { return name; }
    @Override
    public NodeType getType() { return type; }
    @Override
    public List<WorkflowNode> getNextNodes() { return nextNodes; }
    @Override
    public void addIncomingNode(WorkflowNode node) {
        incomingNodes.add(node);
    }
    
    @Override
    public abstract CompletableFuture<Map<String, Object>> execute(String sessionId, Map<String, Object> inputData);
}