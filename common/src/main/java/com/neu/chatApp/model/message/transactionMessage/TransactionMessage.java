package com.neu.chatApp.model.message.transactionMessage;

import com.neu.chatApp.model.message.MessageType;
import com.neu.chatApp.model.message.Message;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.message.joinAndLeaveMessage.JoinAndLeaveMessageType;
import lombok.Data;
import lombok.ToString;

/*
所以说p2p client端发送message的时候里面就转为了包含这几个信息的transaction message 吗？？？
没见到有发送消息的时候使用这个class啊
*/
@Data
@ToString
public class TransactionMessage extends Message {

    private JoinAndLeaveMessageType mainType;

    private TransactionMessageType subType;

    private Node nodeInfo;

    public TransactionMessage(MessageType messageType, TransactionMessageType subType) {
        super(messageType);
        this.subType = subType;
    }

    public TransactionMessage(MessageType messageType, JoinAndLeaveMessageType mainType, TransactionMessageType subType, Node nodeInfo) {
        super(messageType);
        this.mainType = mainType;
        this.subType = subType;
        this.nodeInfo = nodeInfo;
    }

    public TransactionMessage(MessageType messageType, JoinAndLeaveMessageType mainType, Node nodeInfo) {
        super(messageType);
        this.mainType = mainType;
        this.nodeInfo = nodeInfo;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
