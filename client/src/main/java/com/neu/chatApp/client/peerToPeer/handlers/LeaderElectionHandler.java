package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.common.interfaces.Handler;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.common.model.message.MessageType;
import com.neu.chatApp.common.model.message.leaderElectionMessage.LeaderElectionMessageType;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.common.model.node.NodeChannel;
import com.sun.management.OperatingSystemMXBean;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
public class LeaderElectionHandler implements Handler<LeaderElectionMessage> {
  private final Map<Node, Integer> nodeReportsCollector;

  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public LeaderElectionHandler() {
    this.nodeReportsCollector = new HashMap<>();
    // an asynchronous thread will handle the next step once data collection from all nodes is complete.
    this.performanceAnalyzer();
  }

  @Override
  public void handle(LeaderElectionMessage msg, ChannelHandlerContext ctx) {
    switch (msg.getSubType()) {
      // SERVER_AUTH temporarily designates the current node as the leader and initiates leader election coordination
      case SERVER_REQUEST:
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        // start leader election and report the metadata of the leader node
        ClientData.serverChannel = ctx.channel();
        LeaderElectionMessage leaderRequest = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.LEADER_REQUEST);
        log.info("Received LEADER_ELECTION request from centralServer: " + leaderRequest);
        // if empty list send self
        if (ClientData.clientLiveNodes.size() == 0) {
          ClientData.serverChannel.writeAndFlush(new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.CLIENT_REPORT, ClientData.myNode));
          ClientData.serverChannel = null;
          return;
        }
        startLeaderElection(leaderRequest);
        break;
      // SERVER_AUTH designates this node as the leader
      case SERVER_AUTH:
        log.info("Received " + msg.getSubType() + " from centralServer, this node has become the leader node");
        String leaderToken = msg.getLeaderToken();
        ClientData.myNode.setLeader(true);
        ClientData.leaderNodeToken = leaderToken;
        ClientData.serverChannel = ctx.channel();
        // broadcast to all nodes
        if (ClientData.clientLiveNodes.size() != 0) {
          LeaderElectionMessage leaderInfo = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.LEADER_CHOSEN, ClientData.myNode);
          Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
          while (allNodes.hasNext()) {
            NodeChannel next = allNodes.next();
            log.info("Sent LEADER_CHOSEN message to node: " + next.getNodeId() + ": " + leaderInfo);
            next.getChannel().writeAndFlush(leaderInfo);
          }
        }
        break;
      // LEADER_REQUEST from the leader triggers nodes to report their performance
      case LEADER_REQUEST:
        // send request to all nodes
        log.info("Received " + msg.getSubType() + " from leader node");
        // send my performance weight to the node who started the leader election
        int performance = getPerformance();
        LeaderElectionMessage report = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.NODE_REPORT, ClientData.myNode, performance);
        ctx.channel().writeAndFlush(report);
        break;
      // NODE_REPORT for reporting node performance, stored in nodeReportsCollector
      case NODE_REPORT:
        // collect nodes response
        log.info("Received " + msg.getSubType() + " from" + msg.getNodeInfo() + " with performance points " + msg.getPerformanceWeight());
        // collect nodes report
        nodeReportsCollector.put(msg.getNodeInfo(), msg.getPerformanceWeight());
        break;
      // LEADER_CHOSEN sets the client's chosen leader among live nodes.
      case LEADER_CHOSEN:
        log.info("A leader reported: " + msg.getNodeInfo());
        NodeChannel nodeChannel = ClientData.clientLiveNodes.get(msg.getNodeInfo().getNodeId());
        nodeChannel.setLeader(true);
        break;
    }
  }
  
  /**
   * Only call to send LEADER_REQUEST to start the leader election.
   *
   * @param request the request from centralServer or leader
   */
  private void startLeaderElection(LeaderElectionMessage request) {
    Iterator<NodeChannel> allNodes = ClientData.clientLiveNodes.getAllNodes();
    while (allNodes.hasNext()) {
      NodeChannel next = allNodes.next();
      next.getChannel().writeAndFlush(request);
      log.info("Sent request: " + request + " to node: " + next.getNodeId());
    }
  }

  public void performanceAnalyzer() {
    executorService.scheduleAtFixedRate(() -> {
      if (nodeReportsCollector.size() == ClientData.clientLiveNodes.size() && ClientData.clientLiveNodes.size() != 0) {
        // add self performance
        nodeReportsCollector.put(ClientData.myNode, getPerformance());
        // analyze which node has the lowest performance point
        Map.Entry<Node, Integer> theBestNode = getTheBestNode();
        // send the new leader node information to the centralServer
        LeaderElectionMessage leader = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.CLIENT_REPORT, theBestNode.getKey());
        ClientData.serverChannel.writeAndFlush(leader);
        // centralServer will close the channel
        ClientData.serverChannel = null;
        nodeReportsCollector.clear();
      }
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  public Map.Entry<Node, Integer> getTheBestNode() {
    List<Map.Entry<Node, Integer>> collect = nodeReportsCollector.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).collect(Collectors.toList());
    return collect.get(0);
  }


  /**
   * Get current system performance based on the factors on cpu load, JVM memory.
   *
   * @return an overall weighted points from the system performance in interval 0 - 100, the lower, the better, which means system is less loaded.
   */
  public int getPerformance() {
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    // 35%
    double systemCpuLoad = osBean.getSystemCpuLoad();

    // in unit MB
    // 40%
    long freeMemory = Runtime.getRuntime().freeMemory() / 1024 * 1024;
    long maxMemory = Runtime.getRuntime().maxMemory();

    // 5%
    int availableProcessors = osBean.getAvailableProcessors();

    // 20%
    double systemLoadAverage = osBean.getSystemLoadAverage();

    int result;
    // if the program cannot get cpu usage due to privileges
    if (systemCpuLoad < 0 || systemLoadAverage < 0) {
      log.warn("Cannot get cpu or system loads due to the limited privileges");
      // consider the load is maximum by default
      result = (int) (35 + (freeMemory / maxMemory) * 40 + (1 - availableProcessors / 8) * 5 + 20);
    } else {
      log.info("System cpu load: {}% ", systemCpuLoad * 100);
      log.info("JVM free memory: {}MB", freeMemory);
      log.info("JVM max memory: {}MB", maxMemory);
      log.info("System available processors: " + availableProcessors);
      log.info("System average load: {}%", systemLoadAverage * 100);
      // call calculate the points
      result = (int) (systemCpuLoad * 35 + (freeMemory / maxMemory) * 40 + (1 - availableProcessors / 8) * 5 + systemLoadAverage * 20);
    }
    log.info("My system performance point: " + result);
    return result;
  }
}

