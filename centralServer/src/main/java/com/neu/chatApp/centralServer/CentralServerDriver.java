package com.neu.chatApp.centralServer;

import com.neu.chatApp.centralServer.peerToPeer.SocketDriver;
import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.util.PreConnectionTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.net.InetAddress;

/**
 *
 */
@SpringBootApplication
//@EntityScan(basePackages = "com.neu.chatApp.common.model.user")
@EntityScan(basePackages = "com.neu.chatApp.centralServer.db")

@Slf4j
public class CentralServerDriver implements CommandLineRunner {

    private static int socketPort;

    @Value("${server.port}")
    private int httpPort;

    public static void main(String[] args) {
      if (args.length == 1) {
        try {
          socketPort = Integer.parseInt(args[0]);

          SpringApplication.run(CentralServerDriver.class, args);
        }
        catch (NumberFormatException e) {
          System.out.println("Invalid format of ports");
        }
      }
      else {
        System.out.println("Please specify <p2p socketPort> to start the application");
      }

    }

    @Override
    public void run(String... args) throws Exception {
      ServerData.mySocketPort = socketPort;
      ServerData.myHttpPort = httpPort;
      log.info("Localhost address: " + InetAddress.getLocalHost().getHostAddress());
      // test system socketPort if they are available for the application to start
      boolean nettyPort = PreConnectionTest.testPortAvailable(socketPort);
      if (!nettyPort) {
        log.error("Please try to use another ports to start the application");
        System.exit(1);
      }
      new SocketDriver(socketPort);
  }
}
