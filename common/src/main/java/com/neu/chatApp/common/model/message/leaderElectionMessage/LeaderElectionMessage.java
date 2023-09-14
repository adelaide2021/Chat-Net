package com.neu.chatApp.common.model.message.leaderElectionMessage;

import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.Message;
import com.neu.chatApp.common.model.node.Node;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LeaderElectionMessage extends Message {

    private LeaderElectionMessageType subType;

    private Node nodeInfo;

    private int performanceWeight;

    private String leaderToken;

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType) {
        super(messageType);
        this.subType = subType;
    }

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType, int performanceWeight) {
        super(messageType);
        this.subType = subType;
        this.performanceWeight = performanceWeight;
    }

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType, String leaderToken) {
        super(messageType);
        this.subType = subType;
        this.leaderToken = leaderToken;
    }

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType, Node nodeInfo, String leaderToken) {
        super(messageType);
        this.subType = subType;
        this.nodeInfo = nodeInfo;
        this.leaderToken = leaderToken;
    }

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType, Node nodeInfo) {
        super(messageType);
        this.subType = subType;
        this.nodeInfo = nodeInfo;
    }

    public LeaderElectionMessage(MessageType messageType, LeaderElectionMessageType subType, Node nodeInfo, int performanceWeight) {
        super(messageType);
        this.subType = subType;
        this.nodeInfo = nodeInfo;
        this.performanceWeight = performanceWeight;
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
