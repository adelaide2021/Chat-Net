package com.neu.chatApp.common.model.message.joinAndLeaveMessage;

/*
这个join and leave的是外部新节点，
 */
public enum JoinAndLeaveMessageType {

    //外部节点加入网络
    //leader node使用节点信息启动事务
    // 携带信息是：type subType nodeInfo
    JOIN,

    // carry type + subType + nodeInfo
    // 用于节点将其信息发送到请求加入和离开的节点
    GREETING,


    // 网络中的一个节点打算离开网络
    //leader node使用节点信息启动事务
    // 携带 type subType nodeInfo
    LEAVE,

    // 用于 leader node告诉existing node： transaction结束了，可以安全推出
    // carry type + subType
    LEAVE_OK
}
