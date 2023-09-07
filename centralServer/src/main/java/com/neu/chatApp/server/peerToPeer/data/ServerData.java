package com.neu.chatApp.server.peerToPeer.data;


import com.neu.chatApp.client.peerToPeer.handlers.DispatchHandler;
import com.neu.chatApp.client.peerToPeer.initializer.P2PInitializer;
import com.neu.chatApp.interfaces.LiveNodes;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.node.NodeChannel;

public class ServerData {
    public static LiveNodes<Node> liveNodes;

    public static P2PInitializer initializer;

    public static NodeChannel leaderNode;

    public static int myHttpPort;

    public static int myPort;

    public static void init(int port) {
        liveNodes = new ServerLiveNodes<>();
        initializer = new P2PInitializer(port, new DispatchHandler());
    }
}
