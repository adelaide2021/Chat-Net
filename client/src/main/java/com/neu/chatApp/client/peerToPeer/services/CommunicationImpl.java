package com.neu.chatApp.client.peerToPeer.services;

import com.neu.chatApp.client.peerToPeer.data.ClientData;

import com.neu.chatApp.common.model.node.NodeChannel;
import com.neu.chatApp.common.model.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/*
是当前节点发送信息，单点通信向给定的id发送，或组播发送
 */
@Slf4j
public class CommunicationImpl implements Communication {

  public CommunicationImpl() {
  }

  @Override
  public void send(Long id, Message msg) {
    NodeChannel nodeChannel = ClientData.clientLiveNodes.get(id);
    log.info("Sent message to id: " + id + ", message: " + msg);
    System.out.println(nodeChannel.getChannel());
    nodeChannel.getChannel().writeAndFlush(msg);
  }

  @Override
  public void broadcast(Message msg) {
    Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
    while (allNodes.hasNext()) {
      NodeChannel next = allNodes.next();
      send(next.getNodeId(), msg);
    }
  }
}

