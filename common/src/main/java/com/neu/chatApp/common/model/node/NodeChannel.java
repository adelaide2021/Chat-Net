package com.neu.chatApp.common.model.node;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.ToString;

/*
使用netty，给node弄一个channel
 */
@Data
@ToString
public class NodeChannel extends Node {

    // the io channel of the node
    private Channel channel;

    // get information of the channel of the node
    public NodeChannel(Node node, Channel channel) {
        super(node.getNodeId(), node.getNodeName(), node.isLeader(), node.getHostname(), node.getPort());
        this.channel = channel;
    }

    // set up a new channel for the node
    public NodeChannel(Long userId, String userName, boolean isLeader, String hostname, int port, Channel channel) {
        super(userId, userName, isLeader, hostname, port);
        this.channel = channel;
    }

    @Override
    public Node getNode() {
        return super.getNode();
    }

    @Override
    public int compareTo(Node o) {
        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
