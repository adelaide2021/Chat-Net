package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.communicationMessage.CommunicationMessage;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.common.model.message.transactionMessage.TransactionMessage;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.client.http.ClientAPI;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;
import com.neu.chatApp.common.model.message.Message;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
为什么要用到rest client？不是只负责处理p2p中的server的吗？？？
dispatcher是向inbound handler中添加active/read/inactive netty逻辑的部分
 */
@Slf4j
@ChannelHandler.Sharable
public class DispatchHandler extends SimpleChannelInboundHandler<Message> {
    private final Handler<JoinAndLeaveMessage> joinAndLeaveHandler;
    private final Handler<TransactionMessage> transactionHandler;

    private final Handler<LeaderElectionMessage> leaderElectionHandler;

    private final Handler<CommunicationMessage> communicationHandler;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public DispatchHandler() {
        this.joinAndLeaveHandler = new JoinAndLeaveHandler();
        this.transactionHandler = new TransactionHandler();
        this.leaderElectionHandler = new LeaderElectionHandler();
        this.communicationHandler = new CommunicationHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        switch (msg.getMessageType()) {
            case JOIN_AND_LEAVE:
                joinAndLeaveHandler.handle((JoinAndLeaveMessage) msg, ctx);
                break;
            case TRANSACTION:
                transactionHandler.handle((TransactionMessage) msg, ctx);
                break;
            case LEADER_ELECTION:
                leaderElectionHandler.handle((LeaderElectionMessage) msg, ctx);
                break;
            case COMMUNICATION:
                communicationHandler.handle((CommunicationMessage) msg, ctx);
                break;
        }
    }

    // 不太明白executor service在这里的用处
    // node和channel的区别？最后一个if即使node在transaction只要channel不在就行是吗？？？
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Node channel = getNodeByChannel(ctx.channel());
        if (channel == null) {
            log.info("Channel: " + ctx.channel() + " break the connection");
            return;
        }
        executorService.submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            if (TransactionHandler.transactionMessage == null) {
                crash(channel);
            } else {
                if (!TransactionHandler.transactionMessage.getNodeInfo().equals(channel)) {
                    crash(channel);
                }
            }
        });
    }

    private Node getNodeByChannel(Channel channel) {
        Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
        while (allNodes.hasNext()) {
            NodeChannel next = allNodes.next();
            if (next.getChannel().equals(channel)) {
                return next.getNode();
            }
        }
        return null;
    }


    //如果是leader node crash为什么是log out？不应该是触发选主吗？？？
    private void crash(Node node) {
        if (ClientData.clientLiveNodes.isContain(node.getNodeId())) {
            if (node.isLeader()) {
                log.info("Detected leader node crash: " + node);
                ClientData.clientLiveNodes.remove(node.getNodeId());
            }
            else {
                // if not the leader node
                log.info("Detected a node crash id: " + node.getNodeId() + ", name: " + node.getNodeName());
                if (ClientData.myNode.isLeader()) {
                    // report to server if my node is leader
                    new ClientAPI().logout(Long.valueOf(node.getNodeName()));
                    //new ClientAPI().postForEntity("http://" + ClientData.serverHostname + ":" + SharableResource.serverHTTPPort + "/user/logout", .getId(), Void.class);
                    log.info("Reported to server");
                }
                ClientData.clientLiveNodes.remove(node.getNodeId());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }


}