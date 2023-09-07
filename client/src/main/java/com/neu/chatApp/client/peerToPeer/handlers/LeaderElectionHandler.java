package com.neu.chatApp.client.peerToPeer.handlers;

import com.neu.chatApp.interfaces.Handler;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.model.message.MessageType;
import com.neu.chatApp.model.message.leaderElectionMessage.LeaderElectionMessage;
import com.neu.chatApp.model.message.leaderElectionMessage.LeaderElectionMessageType;
import com.neu.chatApp.model.node.Node;
import com.neu.chatApp.model.node.NodeChannel;
import com.sun.management.OperatingSystemMXBean;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
node reposts collector在干啥？？
 */
@Slf4j
public class LeaderElectionHandler implements Handler<LeaderElectionMessage> {
  private final Map<Node, Integer> nodeReportsCollector;

  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public LeaderElectionHandler() {
    this.nodeReportsCollector = new HashMap<>();
    // an asynchronously thread will handle the next, if the collect from all nodes
    this.performanceAnalyzer();
  }

  @Override
  public void handle(LeaderElectionMessage msg, ChannelHandlerContext ctx) {
    switch (msg.getSubType()) {
      // server request是如果是加进来的第一个节点自动成为leader，如果不是的话即为某个node crash的选主，调用LEADER REQUEST
      case SERVER_REQUEST:
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        // start leader election and report the metadata of the leader node
        ClientData.centralServer = ctx.channel();
        LeaderElectionMessage centralServerRequest = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.LEADER_REQUEST);
        log.info("Received LEADER_ELECTION request from centralServer: " + centralServerRequest);
        // if empty list send self
        if (ClientData.clientLiveNodes.size() == 0) {
          ClientData.centralServer.writeAndFlush(new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.CLIENT_REPORT, ClientData.myNode));
          ClientData.centralServer = null;
          return;
        }
        startLeaderElection(centralServerRequest);
        break;
      // SERVER AUTH意味着我自己这个node成为了leader
      case SERVER_AUTH:
        log.info("Received " + msg.getSubType() + " from centralServer, this node has become the leader node");
        String leaderToken = msg.getLeaderToken();
        ClientData.myNode.setLeader(true);
        ClientData.leaderNodeToken = leaderToken;
        ClientData.centralServer = ctx.channel();
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
      // LEADER REQUEST是收到leader说要选主，所以向每个node发送node  report的消息，下一步node收到node report就会报告自己的performance
      case LEADER_REQUEST:
        // send request to all nodes
        log.info("Received " + msg.getSubType() + " from leader node");
        // send my performance weight to the node who started the leader election
        int performance = getPerformance();
        LeaderElectionMessage report = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.NODE_REPORT, ClientData.myNode, performance);
        ctx.channel().writeAndFlush(report);
        break;
      // NODE REPORT用于node报告本节点的performance存储在nodeReportsCollector中
      case NODE_REPORT:
        // collect nodes response
        log.info("Received " + msg.getSubType() + " from" + msg.getNodeInfo() + " with performance points " + msg.getPerformanceWeight());
        // collect nodes report
        nodeReportsCollector.put(msg.getNodeInfo(), msg.getPerformanceWeight());
        break;
      // LEADER CHOSEN用于在每个client对应的live nodes中set确定好的leader node
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
   * 但上个handle的 server request就是调用，难道不会形成无限循环？？？
   * 因为调用start leader election后又回到handle的SERVER_REQUEST中了啊？？？
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
        // 唯一的问题是send的server到底是哪个，是central server还是leader node的server还是client对应的netty server？？？
        LeaderElectionMessage leader = new LeaderElectionMessage(MessageType.LEADER_ELECTION, LeaderElectionMessageType.CLIENT_REPORT, theBestNode.getKey());
        ClientData.centralServer.writeAndFlush(leader);
        // centralServer will close the channel
        ClientData.centralServer = null;
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

