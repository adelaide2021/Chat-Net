package com.neu.chatApp.centralServer.peerToPeer.data;

import com.neu.chatApp.common.interfaces.LiveNodes;
import com.neu.chatApp.common.model.node.Node;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/*
1、server live nodes这边implement用的是Node，client live nodes用的是Node channel，有什么深意么？？？
 */
public class ServerLiveNodes<T extends Node> implements LiveNodes<T>, Iterable<T> {
    private final TreeSet<T> nodes;

    public ServerLiveNodes() {
        this.nodes = new TreeSet<T>(T::compareTo);
    }

    public Iterator<T> iterator() {
        return nodes.iterator();
    }

    @Override
    public boolean add(T node) {
        if (node == null) {
            return false;
        }
        return nodes.add(node);
    }

    @Override
    public boolean remove(Long id) {
        if (id == null) {
            return false;
        }
        T t = get(id);
        if (t == null) {
            return false;
        }
        nodes.remove(t);
        return true;
    }
    @Override
    public boolean isContain(Long id) {
        if (id == null) {
            return false;
        }
        T t = get(id);
        return t != null;
    }

    @Override
    public T get(Long id) {
        if (id == null) {
            return null;
        }
        List<T> collect = nodes.stream().filter(node -> node.getNodeId().equals(id)).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    @Override
    public T getLeaderNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        List<T> collect = nodes.stream().filter(Node::isLeader).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    @Override
    public Iterator<T> getAllNodes() {
        return iterator();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    /**
     * Get the first none leader node in the list.
     */
    public T getNext() {
        if (nodes.size() == 0) {
            return null;
        }
        List<T> collect = nodes.stream().filter(node -> !node.isLeader()).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }
}
