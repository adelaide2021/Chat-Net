package com.neu.chatApp.common.model.message.joinAndLeaveMessage;

import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.Message;
import com.neu.chatApp.common.model.node.Node;

import lombok.Data;
import lombok.ToString;

/*
负责一个节点进入或离开
 */
@Data
@ToString
public class JoinAndLeaveMessage extends Message {

    private JoinAndLeaveMessageType subType;

    private Node nodeInfo;

    public JoinAndLeaveMessage(MessageType messageType, JoinAndLeaveMessageType subType) {
        super(messageType);
        this.subType = subType;
    }

    public JoinAndLeaveMessage(MessageType messageType, JoinAndLeaveMessageType subType, Node nodeInfo) {
        super(messageType);
        this.subType = subType;
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
