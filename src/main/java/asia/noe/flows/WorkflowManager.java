package asia.noe.flows;

import java.util.*;
import java.util.concurrent.*;

import asia.noe.flows.nodes.WorkflowNode;

/**
 * Quản lý workflow: chạy các node theo cấu trúc được định nghĩa,
 * hỗ trợ chạy đồng thời với ThreadPoolExecutor cấu hình linh hoạt.
 */
public class WorkflowManager {
    // Cấu hình ThreadPoolExecutor để hỗ trợ TPS cao
    private final ExecutorService executor = new ThreadPoolExecutor(
        50,
        500,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(10000)
    );

    /**
     * Chạy workflow từ node khởi đầu với sessionId và dữ liệu đầu vào ban đầu.
     */
    public void executeWorkflow(String sessionId, WorkflowNode startNode) {
        executeNode(sessionId, startNode, new HashMap<>());
    }

    private void executeNode(String sessionId, WorkflowNode node, Map<String, Object> inputData) {
        CompletableFuture<Map<String, Object>> future = node.execute(sessionId, inputData);
        future.thenAcceptAsync(outputData -> {
            if (outputData == null) return;  // Ví dụ: đối với MergeNode chưa đủ dữ liệu
            for (WorkflowNode next : node.getNextNodes()) {
                executor.submit(() -> executeNode(sessionId, next, outputData));
            }
        }, executor);
    }
}