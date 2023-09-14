package com.neu.chatApp.centralServer.client.peerToPeer.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;

/**
 * Start bi-directed peer to peer connections.
 * 2、为什么client和server之间要建立peer to peer？不是有http了吗？？？
 * 答：这个netty中的节点有时充当client有时充当server，与总服务器的server概念不是一个
 *
 */
@Slf4j
public class P2PInitializer {

  private final P2PClient client;

  private final P2PServer server;

  /**
   * Construct a peer to peer connection group for actively start and passively receive the incoming connections.
   *
   * @param port the port of the server to be registered
   * @param channelInboundHandler the task channelInboundHandler to dispatch task to different handlers
   */
  public P2PInitializer(int port, ChannelInboundHandler channelInboundHandler) {
    this.client = new P2PClient(channelInboundHandler);
    this.server = new P2PServer(port, channelInboundHandler);
    log.info("Peer to peer com.neu.chatApp.server.service started");
  }

  /**
   * Synchronously Connect to a remote peer of given hostname and port.
   * Wait until the connection completed.
   * 同步连接到指定主机名和端口的远程对等端。
   * 等待连接完成。
   * @param hostname hostname
   * @param port port
   * @return the channel object of the connected socket
   * @throws SocketTimeoutException when failed to connect within limited time
   * 连接远程对等端，是peer to peer是怎么确定连接哪个peer的？
   */
  public Channel connect(String hostname, int port) throws SocketTimeoutException {
    return client.connectTo(hostname, port);
  }
}
