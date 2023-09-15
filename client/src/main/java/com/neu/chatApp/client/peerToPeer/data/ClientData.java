package com.neu.chatApp.client.peerToPeer.data;

import com.neu.chatApp.client.peerToPeer.handlers.DispatchHandler;
import com.neu.chatApp.client.peerToPeer.initializer.P2PInitializer;
import io.netty.channel.Channel;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;

public class ClientData {

    // leader node server information
    public static Channel serverChannel;

    public static String serverHostname;

    public static int httpServerPort;

    public static int p2pServerPort;

    public static String baseURL;

    public static P2PInitializer p2PInitializer;

    // live nodes information on the client side
    public static ClientLiveNodes<NodeChannel> clientLiveNodes;

    public static Node myNode;

    // server information on the client side
    public static String myHostname;

    public static int myPort;

    public static String leaderNodeToken;


    public static void init(String hostname, int port) {
        myHostname = hostname;
        myPort = port;
        clientLiveNodes = new ClientLiveNodes<>();
        p2PInitializer = new P2PInitializer(port, new DispatchHandler());
    }
}
