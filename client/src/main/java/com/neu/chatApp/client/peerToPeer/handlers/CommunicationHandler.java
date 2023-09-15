package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.communicationMessage.CommunicationMessage;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.util.FormattedPrinter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

// communication handler中只是在print？
@Slf4j
public class CommunicationHandler implements Handler<CommunicationMessage> {

    // 所以这个是收到的msg？比如PRIVATE MESSAGE意思是收到了一条msg吗？
    // broadcast也是收到了一条广播？那是谁发送的呢？
    public CommunicationHandler() {}

    @Override
    public void handle(CommunicationMessage msg, ChannelHandlerContext ctx) {
        switch (msg.getSubType()) {
            case PRIVATE_MESSAGE:
                // look up the sender name
                String senderName = ClientData.clientLiveNodes.get(msg.getSender()).getNodeName();
                String message = FormattedPrinter.formatter(true, msg.getSender(), senderName, msg.getMessageContent());
                FormattedPrinter.printSystemMessage(message);
                break;
            case BROADCAST_MESSAGE:
                FormattedPrinter.printSystemMessage(FormattedPrinter.formatter(true, msg.getMessageContent()));
                break;
        }
    }
}

