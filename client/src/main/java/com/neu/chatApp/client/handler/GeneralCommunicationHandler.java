package com.neu.chatApp.client.handler;

import com.neu.chatApp.client.data.ClientData;

import com.neu.chatApp.formattedPrinter.FormattedPrinter;
import com.neu.chatApp.handlerAPI.GeneralEventHandlerAPI;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import com.neu.chatApp.model.protocol.generalCommunicationProtocol.GeneralCommunicationProtocol;

@Slf4j
public class GeneralCommunicationHandler implements GeneralEventHandlerAPI<GeneralCommunicationProtocol> {


    public GeneralCommunicationHandler() {}

    @Override
    public void handle(GeneralCommunicationProtocol protocol, ChannelHandlerContext ctx) {
        switch (protocol.getSubType()) {
            case PRIVATE_MESSAGE:
                // look up the sender name
                String senderName = ClientData.liveNodeList.get(protocol.getSender()).getUserName();
                String message = FormattedPrinter.formatter(true, protocol.getSender(), senderName, protocol.getMessageContent());
                FormattedPrinter.printSystemMessage(message);
                break;
            case BROADCAST_MESSAGE:
                FormattedPrinter.printSystemMessage(FormattedPrinter.formatter(true, protocol.getMessageContent()));
                break;
        }

    }


}
