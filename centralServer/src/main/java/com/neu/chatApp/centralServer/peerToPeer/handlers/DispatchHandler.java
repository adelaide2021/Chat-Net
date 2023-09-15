package com.neu.chatApp.centralServer.peerToPeer.handlers;

import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.common.model.message.Message;
import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/*
只有read0的时候是用netty分派各种message，如果是关闭channel等操作就用的是http？？？
啊就是说如果是leader node crash了就用http来关闭否则的话一般就用netty？？？
*/
@Slf4j
public class DispatchHandler extends SimpleChannelInboundHandler<Message> {
    private final LeaderElectionHandler leaderElectionHandler;
    private final JoinAndLeaveHandler joinAndLeaveHandler;

    public DispatchHandler() {
        this.leaderElectionHandler = new LeaderElectionHandler();
        this.joinAndLeaveHandler = new JoinAndLeaveHandler();
    }

    /**
     * Message read in with the io channel of the sender.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     * @throws Exception any exception thrown from the method will be caught at exceptionCaught method.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        switch (msg.getMessageType()) {
            case LEADER_ELECTION:
                leaderElectionHandler.handle((LeaderElectionMessage) msg, ctx);
                break;
            case JOIN_AND_LEAVE:
                joinAndLeaveHandler.handle((JoinAndLeaveMessage) msg, ctx);
                break;
        }
    }

    /**
     * A node broke or lost connection.
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ServerData.leaderNode != null && ctx.channel().equals(ServerData.leaderNode.getChannel())) {
            OkHttpClient client = new OkHttpClient();
            String url = "http://localhost:" + ServerData.myHttpPort + "/user/logout";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, ServerData.leaderNode.getNodeId().toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Failed to send HTTP request: " + response.code() + " " + response.message());
                } else {
                    log.info("HTTP request was successful");
                }
            } catch (IOException e) {
                log.error("Failed to send HTTP request: " + e.getMessage());
            }

            log.error("The last leader node logged out triggered by the system");
            ServerData.serverLiveNodes.remove(ServerData.leaderNode.getNodeId());

            log.warn("Leader node lost the connection with the server, a new round leader election will start");
            if (ServerData.serverLiveNodes.size() != 0) {
                Channel channel = leaderElectionHandler.connectToNext();
                // Start a new round leader election
                LeaderElectionMessage leaderElectionRequest = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_REQUEST);
                log.info("Sent request: " + leaderElectionRequest + " to a node");
                channel.writeAndFlush(leaderElectionRequest);
            } else {
                log.info("No nodes are in the p2p network");
            }
        } else {
            super.channelInactive(ctx);
        }
    }

    /**
     * Exceptions caught here.
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }
}
