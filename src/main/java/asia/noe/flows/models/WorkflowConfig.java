package asia.noe.flows.models;

import java.util.List;

import java.util.Map;

public class WorkflowConfig {
    private List<NodeConfig> nodes;
    
    // Định nghĩa connections đúng theo cấu trúc JSON
    private Map<String, Map<String, List<List<Map<String, String>>>>> connections;
    
    private String startNode;

    public List<NodeConfig> getNodes() { return nodes; }
    public void setNodes(List<NodeConfig> nodes) { this.nodes = nodes; }

    public Map<String, Map<String, List<List<Map<String, String>>>>> getConnections() { return connections; }
    public void setConnections(Map<String, Map<String, List<List<Map<String, String>>>>> connections) { this.connections = connections; }

    public String getStartNode() { return startNode; }
    public void setStartNode(String startNode) { this.startNode = startNode; }
}