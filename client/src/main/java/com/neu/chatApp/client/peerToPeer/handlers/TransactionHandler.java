package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.client.peerToPeer.services.Transaction;
import com.neu.chatApp.client.peerToPeer.services.TransactionImpl;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessageType;
import com.neu.chatApp.common.model.message.transactionMessage.TransactionMessage;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.node.NodeChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
调用的是transaction返回的结果，transaction本身返回accept/abort等结果，此类统计结果，总的执行两阶段协议
transaction handler用到transaction同时transaction又用到transaction handler这样行吗？？？
下面到底是write and flush给central centralServer还是centralServer没弄清楚？？？
好像明白了！transaction因为有write and flush就也会调用dispatcher/handler然后 调用transaction handler,
所以只要是这个accept消息是向leader node传输的，最后的结果就是leader node收到了要处理inbound channel，
所以这个时候就是accept++，如果数量到了那么就可以执行commit
dispatcher里面初始化的时候就调用了analyzer，然后channel read0的时候又执行了handle
 */
@Slf4j
public class TransactionHandler implements Handler<TransactionMessage> {

  private final Transaction transaction;

  public static TransactionMessage transactionMessage;

  private int countAccept = 0;
  private int countAbort = 0;
  private int countACKCommit = 0;
  private int countACKDrop = 0;

  private boolean isPhase1Completed;

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

  public TransactionHandler() {
    this.transaction = new TransactionImpl();
    this.phase1Analyzer();
    this.phase2Analyzer();
  }

  // handle中的最后commit就到执行阶段了，
  // 即如果是join就建立新的一组client server的group，如果是leave的话就从live node中删除
  @Override
  public void handle(TransactionMessage message, ChannelHandlerContext ctx) {
    switch (message.getSubType()) {
      case PREPARE:
        switch (message.getMainType()) {
          case JOIN:
            boolean isContain = ClientData.clientLiveNodes.isContain(message.getNodeInfo().getNodeId());
            if (isContain) {
              transaction.abort(ctx.channel());
            } else {
              transaction.accept(ctx.channel());
            }
            break;
          case LEAVE:
            boolean isExisted = ClientData.clientLiveNodes.isContain(message.getNodeInfo().getNodeId());
            if (isExisted) {
              transaction.accept(ctx.channel());
            } else {
              transaction.abort(ctx.channel());
            }
            break;
        }
        break;
      case ACCEPT:
        if (ClientData.myNode.isLeader()) {
          log.info("Received ACCEPT response");
          countAccept++;
        }
        break;
      case ABORT:
        if (ClientData.myNode.isLeader()) {
          log.info("Received ABORT response");
          countAbort++;
        }
        break;
      case COMMIT:
        log.info("Committing the transaction");
        if (JoinAndLeaveMessageType.JOIN.equals(message.getMainType())) {
          connectTo(message);
          // send ack
          transaction.ackCommit(ctx.channel());
        } else if (JoinAndLeaveMessageType.LEAVE.equals(message.getMainType())) {
          // send ack
          transaction.ackCommit(ctx.channel());
          ClientData.clientLiveNodes.remove(message.getNodeInfo().getNodeId());
          log.info("Broke connection with the node: " + message.getNodeInfo());
        }
        break;
      case DROP:
        log.info("Drop the transaction");
        // send ack
        transaction.ackDrop(ctx.channel());
        // do nothing
        break;
      case ACK_COMMIT:
        if (ClientData.myNode.isLeader()) {
          log.info("Received ack commit");
          countACKCommit++;
        }
        break;
      case ACK_DROP:
        if (ClientData.myNode.isLeader()) {
          log.info("Received ack drop");
          countACKDrop++;
        }
        break;
    }
  }

  private void resetPhase1Counter() {
    countAccept = 0;
    countAbort = 0;
  }

  private void resetPhase2Counter() {
    countACKCommit = 0;
    countACKDrop = 0;
    transactionMessage = null;
    isPhase1Completed = false;
    // tell the handler the transaction has done
    JoinAndLeaveHandler.unlock();
  }


  private void phase1Analyzer() {
    executorService.scheduleAtFixedRate(() -> {
      // exclude the current node in transaction and self
      while (transactionMessage != null && !isPhase1Completed) {
        if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.LEAVE)) {
          // if self exit
          if (ClientData.myNode.getNodeId().equals(transactionMessage.getNodeInfo().getNodeId())) {
            if (countAccept + countAbort == ClientData.clientLiveNodes.size()) {
              phase1Processor();
            }
          } else {
            if (countAccept + countAbort == ClientData.clientLiveNodes.size() - 1) {
              phase1Processor();
            }
          }
        } else if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.JOIN)) {
          if (countAccept + countAbort == ClientData.clientLiveNodes.size()) {
            phase1Processor();
          }
        }
      }
    }, 300, 700, TimeUnit.MILLISECONDS);
  }

  private void phase1Processor() {
    log.info("Phase 1 completed");
    if (countAbort > 0) {
      // send drop
      transaction.drop(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
      log.info("Abort message occurred in transaction, a DROP message sent");
      resetPhase1Counter();
      isPhase1Completed = true;
      return;
    }
    // check self
    if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.JOIN)) {
      if (ClientData.clientLiveNodes.isContain(transactionMessage.getNodeInfo().getNodeId())) {
        // drop
        transaction.drop(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
        log.info("Abort message occurred in transaction, a DROP message sent");
      }
      else {
        // commit
        transaction.commit(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
        log.info("No Abort message occurred in transaction, a COMMIT message sent");
      }
    }
    else if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.LEAVE)) {
      // if self exit
      if (ClientData.myNode.getNodeId().equals(transactionMessage.getNodeInfo().getNodeId())) {
          transaction.commit(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
          log.info("No Abort message occurred in transaction, a COMMIT message sent");
        }
      else {
        if (ClientData.clientLiveNodes.isContain(transactionMessage.getNodeInfo().getNodeId())) {
            // commit
            transaction.commit(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
            log.info("No Abort message occurred in transaction, a COMMIT message sent");
        }
        else {
            // drop
            transaction.drop(transactionMessage.getNodeInfo(), transactionMessage.getMainType());
            log.info("Abort message occurred in transaction, a DROP message sent");
        }
      }
    }
    isPhase1Completed = true;
    resetPhase1Counter();
  }

  private void phase2Analyzer() {
    executorService.scheduleAtFixedRate(() -> {
      // exclude the current node in transaction and self
      while (transactionMessage != null && isPhase1Completed) {
        if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.LEAVE)) {
          // if self exit
          if (ClientData.myNode.getNodeId().equals(transactionMessage.getNodeInfo().getNodeId())) {
            if ((countACKCommit == ClientData.clientLiveNodes.size() || countACKDrop == ClientData.clientLiveNodes.size())) {
              phase2Processor();
            }
          } else {
            if ((countACKCommit == ClientData.clientLiveNodes.size() - 1 || countACKDrop == ClientData.clientLiveNodes.size() - 1)) {
              phase2Processor();
            }
          }
        } else if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.JOIN)) {
          if ((countACKCommit == ClientData.clientLiveNodes.size() || countACKDrop == ClientData.clientLiveNodes.size())) {
            phase2Processor();
          }
        }
      }
    }, 300, 700, TimeUnit.MILLISECONDS);
  }

  private void phase2Processor() {
    log.info("Phase 2 completed");
    // do action on self
    if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.JOIN)) {
      connectTo(transactionMessage);
      // report to centralServer
      ClientData.serverChannel.writeAndFlush(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.JOIN, transactionMessage.getNodeInfo()));
    }
    else if (transactionMessage.getMainType().equals(JoinAndLeaveMessageType.LEAVE)) {
      // check if not self
      if (!transactionMessage.getNodeInfo().getNodeId().equals(ClientData.myNode.getNodeId())) {
        // send ack to the exit node
        JoinAndLeaveMessage res = new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE_OK);
        log.info("Sent LEAVE_OK to the exiting node: " + res);
        ClientData.clientLiveNodes.get(transactionMessage.getNodeInfo().getNodeId()).getChannel().writeAndFlush(res);
        log.info("Broke connection with the node: " + transactionMessage.getNodeInfo());
        ClientData.clientLiveNodes.remove(transactionMessage.getNodeInfo().getNodeId());
        // report to centralServer
        ClientData.serverChannel.writeAndFlush(new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.LEAVE, transactionMessage.getNodeInfo()));
      }
      else {
        resetPhase2Counter();
        return;
      }
    }
    resetPhase2Counter();
  }

  private void connectTo(TransactionMessage transactionMessage) {
    try {
      Channel channel = ClientData.p2PInitializer.connect(transactionMessage.getNodeInfo().getHostname(), transactionMessage.getNodeInfo().getPort());
      // add to live node list
      ClientData.clientLiveNodes.add(new NodeChannel(transactionMessage.getNodeInfo(), channel));
      log.info("Established connection with a new node: " + transactionMessage.getNodeInfo());
      // send greeting message
      JoinAndLeaveMessage greeting = new JoinAndLeaveMessage(MessageType.JOIN_AND_LEAVE, JoinAndLeaveMessageType.GREETING, ClientData.myNode);
      log.info("Sent GREETING response for JOIN request of node: " + transactionMessage.getNodeInfo());
      channel.writeAndFlush(greeting);
    } catch (SocketTimeoutException ignored) {
      // the exception shouldn't be happened since the node that requested join and leave should keep connection with the leader node
    }
  }
}
