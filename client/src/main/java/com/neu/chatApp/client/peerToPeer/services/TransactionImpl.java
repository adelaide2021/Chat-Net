package com.neu.chatApp.client.peerToPeer.services;

import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessageType;
import com.neu.chatApp.common.model.message.Message;
import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.transactionMessage.TransactionMessage;
import com.neu.chatApp.common.model.message.transactionMessage.TransactionMessageType;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/*
此类中封装成transaction message类型的消息并向live node broadcast这一message，
即向channel中write and flush
 */
@Slf4j
public class TransactionImpl implements Transaction {

  public TransactionImpl() {}

  @Override
  public void prepare(Node nodeInfo, JoinAndLeaveMessageType type) {
    TransactionMessage message = new TransactionMessage(MessageType.TRANSACTION, type, TransactionMessageType.PREPARE, nodeInfo);
    log.info("Broadcast the transaction prepare message: " + message);
    // 为什么要调用transaction handler中的transaction messgae来封装？？？
    //TransactionHandler.transactionMessage = new TransactionMessage(MessageType.JOIN_AND_LEAVE, type, nodeInfo);
    //log.info("Current node in transaction: " + TransactionHandler.transactionMessage);
    broadcastExclude(message, nodeInfo.getNodeId());
  }

  @Override
  public void accept(Channel channel) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, TransactionMessageType.ACCEPT);
    log.info("transactionMessageponded to the transaction: " + transactionMessage);
    channel.writeAndFlush(transactionMessage);
  }

  @Override
  public void abort(Channel channel) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, TransactionMessageType.ABORT);
    log.info("transactionMessageponded to the transaction: " + transactionMessage);
    channel.writeAndFlush(transactionMessage);
  }

  @Override
  public void commit(Node nodeInfo, JoinAndLeaveMessageType type) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, type, TransactionMessageType.COMMIT, nodeInfo);
    log.info("Sent commit transactionMessage to the transaction: " + transactionMessage);
    broadcastExclude(transactionMessage, nodeInfo.getNodeId());
  }

  @Override
  public void drop(Node nodeInfo, JoinAndLeaveMessageType type) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, type, TransactionMessageType.DROP, nodeInfo);
    log.info("Sent drop transactionMessage to the transaction: " + transactionMessage);
    broadcastExclude(transactionMessage, nodeInfo.getNodeId());
  }

  @Override
  public void ackCommit(Channel channel) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, TransactionMessageType.ACK_COMMIT);
    log.info("transactionMessageponded an ACK message to the transaction: " + transactionMessage);
    channel.writeAndFlush(transactionMessage);
  }

  @Override
  public void ackDrop(Channel channel) {
    TransactionMessage transactionMessage = new TransactionMessage(MessageType.TRANSACTION, TransactionMessageType.ACK_DROP);
    log.info("transactionMessageponded an ACK message to the transaction: " + transactionMessage);
    channel.writeAndFlush(transactionMessage);
  }


  /**
   * Broadcast a message exclude the node with given id.
   *
   * @param msg the message to nodes
   * @param id the id of excluded node
   */
  private void broadcastExclude(Message msg, Long id) {
    Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
    while (allNodes.hasNext()) {
      NodeChannel next = allNodes.next();
      if (!next.getNodeId().equals(id)) {
        log.info("Sent to node [" + next.getNode().getNodeId() + "] " + next.getNode().getNodeName() + ": " + msg);
        next.getChannel().writeAndFlush(msg);
      }
    }
  }
}
