package com.pulzit.discovery.domain;

/**
 * Created by gastonsanguinetti on 07/05/16.
 */
public class Node {

    private long nodeId;
    private NodeData nodeData;
    private NodeLocation nodeLocation;

    public Node (){}

    public Node(long nodeId, NodeData nodeData, NodeLocation nodeLocation) {
        this.nodeId = nodeId;
        this.nodeData = nodeData;
        this.nodeLocation = nodeLocation;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public NodeData getNodeData() {
        return nodeData;
    }

    public void setNodeData(NodeData nodeData) {
        this.nodeData = nodeData;
    }

    public NodeLocation getNodeLocation() {
        return nodeLocation;
    }

    public void setNodeLocation(NodeLocation nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

}
