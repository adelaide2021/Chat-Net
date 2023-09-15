package com.neu.chatApp.centralServer.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;
import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.centralServer.peerToPeer.data.TokenGenerator;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessageType;
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
    public LeaderElectionHandler() {
    }

    @Override
    public void handle(LeaderElectionMessage msg, ChannelHandlerContext ctx) {
        switch (msg.getSubType()) {
            //  TOKEN_RETURN when the current leader exits
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
                // After verification, remove the current leader node
                if (leaderNode.getNodeId().equals(id) && leaderNode.getHostname().equals(hostname) && leaderNode.getPort() == port) {
                    ServerData.serverLiveNodes.remove(leaderNode.getNodeId());
                    ServerData.leaderNode = null;
                    Request request = new Request.Builder()
                            .url("http://localhost:" + ServerData.myHttpPort + "/user/logout")
                            .post(RequestBody.create(MediaType.parse("application/json"), String.valueOf(id)))
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.body() != null) {
                            response.body().close();
                        }
                        client.dispatcher().executorService().shutdown();
                        client.connectionPool().evictAll();
                        ctx.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                log.info("Leader node: " + leaderNode + " exited");
                // start leader election after the leader node exited if the live node list is not empty
                if (ServerData.serverLiveNodes.size() == 0) {
                    log.info("No nodes are in the p2p network");
                    return;
                }
                Channel next = connectToNext();
                next.writeAndFlush(new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_REQUEST));
                break;

            // set the node reported by the client as the new leader node
            case CLIENT_REPORT:
                // get and update the new leader node
                Node newLeader = msg.getNodeInfo();
                // set current leader to false if it has
                if (ServerData.serverLiveNodes.size() != 0) {
                    if (ServerData.serverLiveNodes.getLeaderNode() != null) {
                        Node oldLeader = ServerData.serverLiveNodes.getLeaderNode();
                        oldLeader.setLeader(false);

                        Node node = ServerData.serverLiveNodes.get(newLeader.getNodeId());
                        node.setLeader(true);
                        newLeader = node;
                    } else {
                        // query the current node list and set the node to leader
                        Node node = ServerData.serverLiveNodes.get(newLeader.getNodeId());
                        node.setLeader(true);
                    }
                } else {
                    // if empty then add the node
                    newLeader.setLeader(true);
                    ServerData.serverLiveNodes.add(newLeader);
                }
                log.info("A new leader reported: " + newLeader);
                // close the current connection
                ctx.channel().close();
                String token = TokenGenerator.generateToken(newLeader.getNodeId(), newLeader.getHostname(), newLeader.getPort());
                // connect to the new leader node
                Channel leaderChannel = null;
                try {
                    leaderChannel = ServerData.initializer.connect(newLeader.getHostname(), newLeader.getPort());
                } catch (SocketTimeoutException e) {
                    log.error("Failed to connect to the new leader node: " + newLeader);
                    log.warn("Retry to connect to the new leader node: " + newLeader);
                    // retry once
                    try {
                        leaderChannel = ServerData.initializer.connect(newLeader.getHostname(), newLeader.getPort());
                    } catch (SocketTimeoutException ex) {
                        log.error("Failed to connect the new leader node: " + newLeader + " after retry");
                        // ask another node to start new round leader election
                        Channel channel = connectToNext();
                        if (channel != null) {
                            log.info("Leader election request sent to another node");
                            LeaderElectionMessage newRequest = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.SERVER_REQUEST);
                            channel.writeAndFlush(newRequest);
                        } else {
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
    public Channel connectToNext() {
        if (ServerData.serverLiveNodes.size() == 0) {
            return null;
        }
        Iterator<Node> allNodes = ServerData.serverLiveNodes.getAllNodes();
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
