package asia.noe.flows.nodes.impl;

import java.util.*;
import java.util.concurrent.*;

import asia.noe.flows.NodeType;
import asia.noe.flows.nodes.BaseNode;
import asia.noe.flows.nodes.WorkflowNode;

/**
 * Node hợp nhất dữ liệu từ nhiều nhánh (có nhiều đầu vào).
 * Khi số lượng input đạt được số lượng mong đợi (expectedInputs) thì gộp dữ liệu và trả về.
 */
public class MergeNode extends BaseNode {
    private final ConcurrentHashMap<String, List<Map<String, Object>>> sessionData = new ConcurrentHashMap<>();
    private final int expectedInputs;

    public MergeNode(String id, String name, int expectedInputs, List<WorkflowNode> nextNodes) {
        super(id, name, NodeType.MERGE, nextNodes);
        this.expectedInputs = expectedInputs;
    }

    @Override
    public synchronized CompletableFuture<Map<String, Object>> execute(String sessionId, Map<String, Object> inputData) {
        return CompletableFuture.supplyAsync(() -> {
            sessionData.putIfAbsent(sessionId, new ArrayList<>());
            sessionData.get(sessionId).add(inputData);
            if (sessionData.get(sessionId).size() < expectedInputs) {
                return null;
            }
            Map<String, Object> mergedData = new HashMap<>();
            for (Map<String, Object> data : sessionData.remove(sessionId)) {
                mergedData.putAll(data);
            }
            return mergedData;
        });
    }
}
