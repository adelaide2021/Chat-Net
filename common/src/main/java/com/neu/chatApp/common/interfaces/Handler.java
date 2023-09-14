package com.neu.chatApp.common.interfaces;

import com.neu.chatApp.common.model.message.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * A general event handler api handle the events that extends the top protocol.
 *
 * @param <T> the protocol that extends the TransmitProtocol
 */
public interface Handler<T extends Message> {

  /**
   * Handle the incoming event.
   *
   * @param message the message
   * @param ctx channel context
   */
  void handle(T message, ChannelHandlerContext ctx);
}
