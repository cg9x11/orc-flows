package asia.noe.flows;
import java.util.UUID;

import asia.noe.flows.nodes.WorkflowNode;

public class Main {
    public static void main(String[] args) {
        try {
            // Đường dẫn tới file cấu hình JSON
            String configFilePath = "src/main/resources/workflow.json";
            WorkflowNode startNode = WorkflowBuilder.buildWorkflow(configFilePath);
            // Tạo sessionId duy nhất cho luồng xử lý
            String sessionId = UUID.randomUUID().toString();
            WorkflowManager workflowManager = new WorkflowManager();
            workflowManager.executeWorkflow(sessionId, startNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
