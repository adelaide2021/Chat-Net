package com.neu.chatApp.client.peerToPeer.data;

import com.neu.chatApp.common.interfaces.LiveNodes;
import io.netty.channel.Channel;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * List of client live node
 */
public class ClientLiveNodes<T extends NodeChannel> implements LiveNodes<T>, Iterable<T> {

    private final TreeSet<T> nodes;

    public ClientLiveNodes() {
        this.nodes = new TreeSet<>(T::compareTo);
    }


    public synchronized boolean add(T node) {
        if (node == null) {
            return false;
        }
        return nodes.add(node);
    }


    public synchronized boolean remove(Long nodeId) {
        if (nodeId == null) {
            return false;
        }
        T t = get(nodeId);
        if (t == null) {
            return false;
        }
        Channel channelToBeRemoved = t.getChannel();
        nodes.remove(t);
        if (channelToBeRemoved != null) {
            // close the channel with all handler resources associated with the channel
            channelToBeRemoved.close();
        }
        return true;
    }


    public synchronized boolean isContain(Long userId) {
        if (userId == null) {
            return false;
        }
        T t = get(userId);
        return t != null;
    }

    // get这个T泛型是get啥？？？难道不应该返回的node吗，还是说可以返回node或者node channel？？？
    // T为什么要extend node channel，而不直接用node channel？？？
    public synchronized T get(Long nodeId) {
        if (nodeId == null) {
            return null;
        }
        List<T> collect = nodes.stream().filter(node -> node.getNodeId().equals(nodeId)).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    //
    public synchronized T getLeaderNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        List<T> collect = nodes.stream().filter(Node::isLeader).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }


    public synchronized Iterator<T> getAllNodes() {
        return iterator();
    }


    public synchronized int size() {
        return nodes.size();
    }


    public synchronized T getNext() {
        if (nodes.size() == 0) {
            return null;
        }
        List<T> collect = nodes.stream().filter(node -> !node.isLeader()).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    @Override
    public synchronized Iterator<T> iterator() {
        return nodes.iterator();
    }
}