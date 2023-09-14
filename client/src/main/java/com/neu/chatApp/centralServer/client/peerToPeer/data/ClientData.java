package com.neu.chatApp.centralServer.client.peerToPeer.data;

import com.neu.chatApp.centralServer.client.peerToPeer.handlers.DispatchHandler;
import com.neu.chatApp.centralServer.client.peerToPeer.initializer.P2PInitializer;
import io.netty.channel.Channel;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;

/*
// 要不要接数据库
 */

public class ClientData {
    // 本client对应的p2p server的信息
    public static Channel serverChannel;
    public static String serverHostname;
    // 为什么当前node对应的server还会有http的port？？？
    public static int httpServerPort;
    public static int p2pServerPort;

    public static String baseURL;

    public static P2PInitializer p2PInitializer;

    // node信息（为什么要存储node，node和client的关系到底是什么？？？）
    // 为什么要重读存储信息？？？
    public static ClientLiveNodes<NodeChannel> clientLiveNodes;
    public static Node myNode;

    public static String myHostname;

    public static int myPort;
    //这个leader node token每个节点都要保存一个是在干嘛？？？
    public static String leaderNodeToken;


    // 同时没有用过哇？？？
    public static void init(String hostname, int port) {
        myHostname = hostname;
        myPort = port;
        clientLiveNodes = new ClientLiveNodes<>();
        // 10、client dispatcher是啥？？？
        p2PInitializer = new P2PInitializer(port, new DispatchHandler());
    }
}
