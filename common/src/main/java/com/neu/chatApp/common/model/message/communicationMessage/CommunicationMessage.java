package com.neu.chatApp.common.model.message.communicationMessage;

import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.Message;

import lombok.Data;
import lombok.ToString;

/*
实际进行 点对点通信/组播通信 的协议
传入sender+receiver+message
 */
@Data
@ToString
public class CommunicationMessage extends Message {

    private CommunicationMessageType subType;
    // 2、sender receiver从何而来，又是是什么？？？
    private Long sender;
    private Long receiver;
    private String messageContent;

    // 3、为什么需要在此处调用transmit type即最上层的type？？？
    // 4. protocol只是将各种值初始化了而已，并没有其他实际的操作？有什么意义吗？？？
    public CommunicationMessage(MessageType messageType, CommunicationMessageType subType, Long sender, String messageContent) {
        super(messageType);
        this.subType = subType;
        this.sender = sender;
        this.messageContent = messageContent;
    }

    public CommunicationMessage(MessageType messageType, CommunicationMessageType subType, Long sender, Long receiver, String messageContent) {
        super(messageType);
        this.subType = subType;
        this.sender = sender;
        this.receiver = receiver;
        this.messageContent = messageContent;
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
