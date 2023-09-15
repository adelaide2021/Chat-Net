package com.neu.chatApp.client.peerToPeer.initializer;

import com.neu.chatApp.common.model.message.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * The netty server class is to passively accept connections from others.
 * And add the connected channel to live node list for future manage.
 */
@Slf4j
public class P2PServer {

  private final int port;

  private final ChannelInboundHandler dispatcher;

  public P2PServer(int port, ChannelInboundHandler dispatcher) {
    this.port = port;
    this.dispatcher = dispatcher;
    log.info("Netty server starting at port: [{}]", port);
    new Thread(this::run).start();
  }


  /**
   * Start the manager server and accept for connection.
   *
   */
  public void run() {
    // create boss and worker threads
    EventLoopGroup boss = new NioEventLoopGroup();
    EventLoopGroup worker = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(boss, worker)
              // nio to accept connection
              .channel(NioServerSocketChannel.class)
              // the maximum queue length for incoming connection
              .option(ChannelOption.SO_BACKLOG, 1024)
              .option(ChannelOption.SO_REUSEADDR, true)
              .childOption(ChannelOption.SO_KEEPALIVE, true)
              .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ChannelPipeline pipeline = ch.pipeline();
                  // add byte encoder and decoder
                  pipeline.addLast(new ObjectDecoder(1024*1024,
                                  ClassResolvers.weakCachingConcurrentResolver(Message.class.getClassLoader())))
                          .addLast(new ObjectEncoder())
                          // add task dispatcher
                          .addLast(dispatcher);

                }
              });
      // synchronously bind port to the server until succeed or failed
      ChannelFuture channelFuture = bootstrap.bind(port).sync();
      // listening channel shutdown
      channelFuture.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      log.error(e.getMessage());
    } finally {
      // on exit, release system resource
      boss.shutdownGracefully();
      worker.shutdownGracefully();
    }
  }
}
