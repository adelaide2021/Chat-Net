package com.neu.chatApp.server.peerToPeer.handlers;

import com.neu.chatApp.interfaces.Handler;
import com.neu.chatApp.model.message.MessageType;
import com.neu.chatApp.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.model.message.leaderElectionMessage.LeaderElectionMessageType;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.node.NodeChannel;
import com.neu.chatApp.server.peerToPeer.data.ServerData;
import com.neu.chatApp.server.peerToPeer.data.TokenGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;

/*

 */
@Slf4j
public class LeaderElectionHandler implements Handler<LeaderElectionMessage> {
    public LeaderElectionHandler() {}

    // 所以token return是leader node return了一个新token触发新一轮选主？？？
    @Override
    public void handle(LeaderElectionMessage msg, ChannelHandlerContext ctx) {
        switch (msg.getSubType()) {
            case TOKEN_RETURN:
                log.info("Leader node is returning the leader token: " + msg.getNodeInfo() + ", token: " + msg.getLeaderToken());
                String leaderToken = msg.getLeaderToken();
                // verify the token if it is the right leader
                Map<String, Object> verify = TokenGenerator.verify(leaderToken);
                if (verify == null) {
                    log.error("Failed to verify the leader token: " + msg.getNodeInfo() + ", token: " + msg.getLeaderToken());
                    // incorrect token
                    // close the connection
                    ctx.channel().close();
                    return;
                }
                // compare the node information
                Long id = (Long) verify.get("id");
                String hostname = (String) verify.get("hostname");
                int port = (int) verify.get("port");
                // get the leader node
                NodeChannel leaderNode = ServerData.leaderNode;

                OkHttpClient client = new OkHttpClient();

                if (leaderNode.getNodeId().equals(id) && leaderNode.getHostname().equals(hostname) && leaderNode.getPort() == port) {
                    // 验证正确后，移除当前领导节点
                    ServerData.liveNodes.remove(leaderNode.getNodeId());
                    ServerData.leaderNode = null;
                    // 创建一个Request对象，设置请求的URL和HTTP方法
                    Request request = new Request.Builder()
                            .url("http://localhost:" + ServerData.myHttpPort + "/user/logout")
                            .post(RequestBody.create(MediaType.parse("application/json"), String.valueOf(id)))
                            .build();
                    try {
                        // 执行HTTP请求
                        Response response = client.newCall(request).execute();
                        // 确保关闭响应体
                        if (response.body() != null) {
                            response.body().close();
                        }
                        // 关闭OkHttpClient
                        client.dispatcher().executorService().shutdown();
                        client.connectionPool().evictAll();
                        // 关闭Netty通道
                        ctx.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                log.info("Leader node: " + leaderNode + " exited");
                // start leader election after the leader node exited if the live node list is not empty
                if (ServerData.liveNodes.size() == 0) {
                    log.info("No nodes are in the p2p network");
                    return;
                }
                Channel next = ConnectToNext();
                next.writeAndFlush(new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_REQUEST));
                break;
            // 设置client report的node为新的leader node
            // 为什么只有在live nodes为空的时候才加入live nodes,是因为其他时候已经加入了？理论上说不是server这边只会维护leader node一个node吗？
            case CLIENT_REPORT:
                // get and update the new leader node
                Node newLeader = msg.getNodeInfo();
                // set current leader to false if it has
                if (ServerData.liveNodes.size() != 0) {
                    if (ServerData.liveNodes.getLeaderNode() != null) {
                        Node oldLeader = ServerData.liveNodes.getLeaderNode();
                        oldLeader.setLeader(false);

                        Node node = ServerData.liveNodes.get(newLeader.getNodeId());
                        node.setLeader(true);
                        newLeader = node;
                    }
                    else {
                        // query the current node list and set the node to leader
                        Node node = ServerData.liveNodes.get(newLeader.getNodeId());
                        node.setLeader(true);
                    }
                }
                else {
                    // if empty then add the node
                    newLeader.setLeader(true);
                    ServerData.liveNodes.add(newLeader);
                }
                log.info("A new leader reported: " + newLeader);
                // close the current connection
                ctx.channel().close();
                String token = TokenGenerator.generateToken(newLeader.getNodeId(), newLeader.getHostname(), newLeader.getPort());
                // connect to the new leader node
                Channel leaderChannel = null;
                try {
                    leaderChannel = ServerData.initializer.connect(newLeader.getHostname(), newLeader.getPort());
                }
                catch (SocketTimeoutException e) {
                    log.error("Failed to connect to the new leader node: " + newLeader);
                    log.warn("Retry to connect to the new leader node: " + newLeader);
                    // retry once
                    try {
                        leaderChannel = ServerData.initializer.connect(newLeader.getHostname(), newLeader.getPort());
                    }
                    catch (SocketTimeoutException ex) {
                        log.error("Failed to connect the new leader node: " + newLeader + " after retry");
                        // ask another node to start new round leader election
                        Channel channel = ConnectToNext();
                        if (channel != null) {
                            log.info("Leader election request sent to another node");
                            LeaderElectionMessage newRequest = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_REQUEST);
                            channel.writeAndFlush(newRequest);
                        }
                        else {
                            // all nodes are unresponsive report system issue
                            log.error("All nodes are unresponsive, please check system issue");
                            return;
                        }
                    }
                }
                assert leaderChannel != null;
                // send token to the new leader node
                LeaderElectionMessage newLeaderMessage = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_AUTH, token);
                leaderChannel.writeAndFlush(newLeaderMessage);
                ServerData.leaderNode = new NodeChannel(newLeader, leaderChannel);
                log.info("Sent token to new leader node: " + newLeader + ", token: " + newLeaderMessage);
                break;
            }
        }
            /**
             * Keep connect to the next node until the connection is established.
             *
             * @return the io channel of the connected node, if run out of all tries or empty node list return null
             */
            public Channel ConnectToNext() {
                if (ServerData.liveNodes.size() == 0) {
                    return null;
                }
                Iterator<Node> allNodes = ServerData.liveNodes.getAllNodes();
                while (allNodes.hasNext()) {
                    Node next = allNodes.next();
                    try {
                        return ServerData.initializer.connect(next.getHostname(), next.getPort());
                    } catch (SocketTimeoutException e) {
                        log.error("Failed to connect to node: " + next);
                    }
                }
                return null;
            }
        }
    }
}
