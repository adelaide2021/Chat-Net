package com.neu.chatApp.common.model.message.transactionMessage;

/*
我理解的是执行两阶段通信的 transaction 的type，
5、只用于辅助 leader election选主的吗，还是执行广泛的两阶段协议？？？
 */
public enum TransactionMessageType {

    //领导节点向所有节点发送准备消息以启动事务
    // carry type + join/leave type + subType + nodeInfo (the node in join or leave event)
    PREPARE,

    // 节点用ACCEPT 表明接受新的节点
    // carry type + join/leave type + subType
    ACCEPT,

    // 节点用ABORT 表明不能接收新的节点
    // carry type + join/leave type + subType
    ABORT,

    // 如果没有出现abort消息，则leader节点发送commit消息来commit
    // carry type + join/leave type + subType + nodeInfo (the node in join or leave event)
    COMMIT,

    // 如果出现abort，则leader节点发送drop
    // carry type + join/leave type + subType + nodeInfo
    DROP,

    // 节点使用ACK_COMMIT来表明commit完成
    // carry type + join/leave type + subType
    ACK_COMMIT,

    // 节点使用ACK_DROP来表明drop完成
    // carry type + join/leave type + subType
    ACK_DROP


}
