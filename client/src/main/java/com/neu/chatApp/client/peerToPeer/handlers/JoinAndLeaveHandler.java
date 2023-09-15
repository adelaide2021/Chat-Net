package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.client.peerToPeer.services.Transaction;
import com.neu.chatApp.client.peerToPeer.services.TransactionImpl;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessageType;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.client.peerToPeer.data.ClientData;

import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.node.NodeChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 */
@Slf4j
public class JoinAndLeaveHandler implements Handler<JoinAndLeaveMessage> {

  private static final Transaction TRANSACTION = new TransactionImpl();

  // （理论上下面两个可以用Kafka代替这个队列，但这个队列是用在本机上也存储在本机上的，Kafka是用在分布式系统中的）
  private static final Queue<JoinAndLeaveMessage> queue = new LinkedBlockingQueue<>();

  private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  private static boolean lock = false;

  public JoinAndLeaveHandler() {
    this.scheduler();
  }


  /**
   * Use for a new none leader node to join the network
   */
  public static void join(String leaderHostname, int leaderPort) throws SocketTimeoutException {
    JoinAndLeaveMessage joinMessage = new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.JOIN, ClientData.myNode);
    Channel connect = ClientData.p2PInitializer.connect(leaderHostname, leaderPort);
    log.info("Sent join request to the leader node: " + joinMessage);
    connect.writeAndFlush(joinMessage);
    connect.close();
  }

  /**
   * Use for the node to leave the network. Called in onExit method.
   */
  public static void leave() {
    // if the node is not leader, send message to the leader
    if (!ClientData.myNode.isLeader()) {
      // send message to the leader node to report leave
      NodeChannel leaderNode = ClientData.clientLiveNodes.getLeaderNode();
      JoinAndLeaveMessage leave = new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE, ClientData.myNode);
      log.info("Sent LEAVE request to the leader node: " + leave);
      leaderNode.getChannel().writeAndFlush(leave);
      return;
    }
    // if the node is leader, start transaction if list is not empty
    if (ClientData.clientLiveNodes.size() > 0) {
      queue.add(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE, ClientData.myNode));
    } else {
       // else just exit
       // CmdLineUI.isLeft = true;
    }
  }

  public void handle(JoinAndLeaveMessage joinAndLeaveMessage, ChannelHandlerContext ctx) {
    // only leader node should take care of join and leave events of a node
    switch (joinAndLeaveMessage.getSubType()) {
      case JOIN:
        if (ClientData.myNode.isLeader()) {
          if (ClientData.clientLiveNodes.size() != 0) {
            // enqueue the transaction
            queue.add(joinAndLeaveMessage);
            if (ctx != null) {
              ctx.channel().close();
            }
          } else {
            // if no nodes in the list, no transaction need
            try {
              Channel connect = ClientData.p2PInitializer.connect(joinAndLeaveMessage.getNodeInfo().getHostname(), joinAndLeaveMessage.getNodeInfo().getPort());
              ClientData.clientLiveNodes.add(new NodeChannel(joinAndLeaveMessage.getNodeInfo(), connect));
              connect.writeAndFlush(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.GREETING, ClientData.myNode));
              log.info("Sent GREETING response for JOIN request of node: " + joinAndLeaveMessage.getNodeInfo());
              // report to server
              ClientData.serverChannel.writeAndFlush(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.JOIN, joinAndLeaveMessage.getNodeInfo()));
              log.info("Sent REPORT to server for node: " + joinAndLeaveMessage.getNodeInfo());
            } catch (SocketTimeoutException ignored) {}
          }
        }
        break;
      case LEAVE:
        if (ClientData.myNode.isLeader()) {
          // if current list contains at least one node that is not the node (transaction object)
          // then start a transaction
          // otherwise report to the server directly and remove the node
          if (ClientData.clientLiveNodes.size() > 1) {
            // enqueue the transaction
            queue.add(joinAndLeaveMessage);
          } else {
            // if the exit node not crash
            if (ctx != null) {
              // send ack to the exit node
              JoinAndLeaveMessage message = new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE_OK);
              log.info("Sent LEAVE_OK to the exiting node: " + message);
              ctx.channel().writeAndFlush(message);
            }
            log.info("Broke connection with the node: " + joinAndLeaveMessage.getNodeInfo());
            ClientData.clientLiveNodes.remove(joinAndLeaveMessage.getNodeInfo().getNodeId());
            // report to server
            ClientData.serverChannel.writeAndFlush(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE, joinAndLeaveMessage.getNodeInfo()));
          }
        }
        break;
      case GREETING:
        if (ClientData.clientLiveNodes.size() == 0) {
          log.info("Joined to the p2p network");
          // UI.isJoined = true;
        }
        // store the node to local live node list
        ClientData.clientLiveNodes.add(new NodeChannel(joinAndLeaveMessage.getNodeInfo(), ctx.channel()));
        break;
      case LEAVE_OK:
        log.info("Received LEAVE_OK, system exited");
        // UI.isLeft = true;
        break;
    }
  }

  public static void unlock() {
    lock = false;
    log.info("Transaction completed");
  }

  /**
   * Join and leave messages are queued and processed in the scheduler,
   * preventing interference with the two-phase protocol's lock period.
   */
  private void scheduler() {
    executorService.scheduleAtFixedRate(() -> {
      while (!queue.isEmpty() && !lock) {
        JoinAndLeaveMessage head = queue.poll();
        lock = true;
        switch (head.getSubType()) {
          case JOIN:
            // start a transaction
            log.info("Start a JOIN transaction for node: " + head.getNodeInfo());
            TRANSACTION.prepare(head.getNodeInfo(), JoinAndLeaveMessageType.JOIN);
            break;
          case LEAVE:
            // start a transaction
            log.info("Start a LEAVE transaction for node: " + head.getNodeInfo());
            TRANSACTION.prepare(head.getNodeInfo(), JoinAndLeaveMessageType.LEAVE);
            break;
        }
      }
    }, 300, 1000, TimeUnit.MILLISECONDS);
  }
}
