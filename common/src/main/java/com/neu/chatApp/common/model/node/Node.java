package com.neu.chatApp.common.model.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/*
记录node的信息，包括name, hostname, port, id等等
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)

public class Node implements Comparable<Node>, Serializable {
    // 7、这个serialize到底在干啥？？？再写进数据库吧？
    private static final long serialVersionUID = 1234567L;

    // userid is automatically generated by the database
    @NotNull
    protected Long nodeId;

    //着实不理解为什么这里需要node name？？？不应该记录在比如client 上边吗？？？
    // 1、最重要的是，这里也有一个host name和port？那client中的my host name port又是啥？？？
    protected String nodeName;

    // is the leader node or not
    protected boolean isLeader;

    // the node hostname
    protected String hostname;

    // the node port
    protected int port;

    public Node getNode() {
        return this;
    }

    @Override
    public int compareTo(@NotNull Node o) {
        return nodeId.compareTo(o.getNodeId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return nodeId.equals(node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
