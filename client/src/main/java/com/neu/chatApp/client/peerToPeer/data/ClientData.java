package com.neu.chatApp.client.peerToPeer.data;

import com.neu.chatApp.client.peerToPeer.initializer.P2PInitializer;
import com.neu.chatApp.client.peerToPeer.handlers.DispatchHandler;
import io.netty.channel.Channel;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.node.NodeChannel;

/*
// 此类中初始化了 p2p，http等所有需要的变量
// 我觉得这个就是sharable resource，每个client记录所有的这些信息
// 9、所以是有一个server的通信信道，一个http 总server，和一个netty的p2pserver？？？
// 要不要接数据库
 */

public class ClientData {
    // central server的信息?
    //这个是central server的信息还是leader node 的信息？？？
    public static Channel centralServer;
    public static String centralServerHostname;
    public static int centralServerPort;
    public static String baseURL;

    // p2p的信息（p2p server port不是应该和my port一样吗？？？）
    public static int p2pServerPort;
    public static P2PInitializer p2PInitializer;

    // node自己的信息（为什么要存储node，node和client的关系到底是什么？？？）
    public static ClientLiveNodes<NodeChannel> clientLiveNodes;
    public static Node myNode;

    public static String myHostname;

    public static int myPort;
    //这个leader node token每个节点都要保存一个是在干嘛？？？
    public static String leaderNodeToken;

    public static void init(String hostname, int port) {
        myHostname = hostname;
        myPort = port;
        clientLiveNodes = new ClientLiveNodes<>();
        // 10、client dispatcher是啥？？？
        p2PInitializer = new P2PInitializer(port, new DispatchHandler());
    }
}
