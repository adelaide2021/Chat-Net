package com.neu.chatApp.model.protocol.joinAndLeaveProtocol;

import com.neu.chatApp.model.protocol.GeneralType;
import com.neu.chatApp.model.protocol.TransmitProtocol;
import com.neu.chatApp.model.node.Node;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JoinAndLeaveProtocol extends TransmitProtocol {

    private JoinAndLeaveType subType;

    private Node nodeInfo;

    public JoinAndLeaveProtocol(GeneralType type, JoinAndLeaveType subType) {
        super(type);
        this.subType = subType;
    }

    public JoinAndLeaveProtocol(GeneralType type, JoinAndLeaveType subType, Node nodeInfo) {
        super(type);
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
