package com.neu.chatApp.common.model.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * 将通过网络传输的主要协议。
 * 有一个任务的通用类型，调度程序可以将任务分派给不同的处理程序。
 * 子协议应扩展协议。
 */
@Data
@NoArgsConstructor
@ToString
public class Message implements Serializable {

    // 1、序列化干啥的？？？
    private static final long serialVersionUID = 1234567L;

    protected MessageType messageType;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message that = (Message) o;
        return messageType == that.messageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType);
    }
}
