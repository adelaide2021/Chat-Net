package com.neu.chatApp.client.handler;

import com.neu.chatApp.handlerAPI.GeneralEventHandlerAPI;
import io.netty.channel.ChannelHandlerContext;

import com.neu.chatApp.model.protocol.leaderElectionProtocol.LeaderElectionProtocol;

public class LeaderElectionHandler implements GeneralEventHandlerAPI<LeaderElectionProtocol> {

  @Override
  public void handle(LeaderElectionProtocol protocol, ChannelHandlerContext ctx) {

  }
}