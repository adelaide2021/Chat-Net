package com.neu.chatApp.centralServer.peerToPeer.data;


import com.neu.chatApp.centralServer.peerToPeer.handlers.DispatchHandler;
import com.neu.chatApp.client.peerToPeer.initializer.P2PInitializer;
import com.neu.chatApp.common.interfaces.LiveNodes;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;

// 考虑把initializer转移到公共文件夹中
public class ServerData {
    public static int myHttpPort;

    public static int mySocketPort;

    public static LiveNodes<Node> serverLiveNodes;

    public static P2PInitializer initializer;

    public static NodeChannel leaderNode;

    public static void init(int port) {
        serverLiveNodes = new ServerLiveNodes<>();
        initializer = new P2PInitializer(port, new DispatchHandler());
    }
}
