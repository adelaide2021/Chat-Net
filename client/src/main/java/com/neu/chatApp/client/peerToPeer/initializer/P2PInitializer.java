package com.neu.chatApp.client.peerToPeer.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;

/**
 * Start bi-directed peer to peer connections.
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
   * @param hostname hostname
   * @param port port
   * @return the channel object of the connected socket
   * @throws SocketTimeoutException when failed to connect within limited time
   */
  public Channel connect(String hostname, int port) throws SocketTimeoutException {
    return client.connectTo(hostname, port);
  }
}
