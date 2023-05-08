package com.neu.chatApp.client.data;

import com.neu.chatApp.client.dispatcher.ClientDispatcher;
import io.netty.channel.Channel;
import com.neu.chatApp.model.liveNodeList.ClientLiveNodeList;
import com.neu.chatApp.model.liveNodeList.LiveNodeList;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.node.NodeChannel;
import com.neu.chatApp.p2pConnectionGroup.P2PConnectionGroup;

public class ClientData {
    public static Channel server;
    public static String serverHostname;
    public static int serverHttpPort;
    public static int serverNettyPort;
    public static P2PConnectionGroup group;

    public static String baseURL;

    public static LiveNodeList<NodeChannel> liveNodeList;
    public static Node myNode;

    public static String myHostname;

    public static int myPort;

    public static void init(String hostname, int port) {
        myHostname = hostname;
        myPort = port;
        liveNodeList = new ClientLiveNodeList<>();
        group = new P2PConnectionGroup(port, new ClientDispatcher());
    }


}
