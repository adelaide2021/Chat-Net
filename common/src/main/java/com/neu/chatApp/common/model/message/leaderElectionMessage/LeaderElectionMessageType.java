package com.neu.chatApp.common.model.message.leaderElectionMessage;


public enum LeaderElectionMessageType {


    // 服务器->客户端
    //用于当leader消失或崩溃，服务器请求一个节点启动leader选举过程
    //carry type + subtype
    SERVER_REQUEST,

    // carry type + subtype + leaderToken
    // server -> client
    // 如果一个节点是第一个加入p2p网络的节点，服务器授权该节点为leader
    SERVER_AUTH,

    // client -> client
    // //当前“leader”节点请求启动leader选举进程
    // carry type + subtype
    LEADER_REQUEST,

    // client -> client
    // 节点向发起选举的节点报告自己的状态
    // carry type + subtype + nodeInfo + performanceWeight
    NODE_REPORT,

    // client -> server
    // leader node离开p2p group 并返回leader token
    // carry type + subtype + nodeInfo + leaderToken
    TOKEN_RETURN,

    // client -> server
    // the node 报告 leader 选举的结果
    // carry type + subtype + nodeInfo
    CLIENT_REPORT,

    // client leader -> client
    // Leader宣布the node为Leader
    // carry type + subtype + nodeInfo
    LEADER_CHOSEN

}
