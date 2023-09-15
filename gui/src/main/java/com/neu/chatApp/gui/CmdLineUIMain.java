package com.neu.chatApp.gui;


import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.util.PreConnectionTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;


@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Slf4j
public class CmdLineUIMain implements CommandLineRunner {

    // server information on the client side
    private static int port;

    // leader node server information
    private static String serverHostname;

    private static int serverHTTPPort;

    private static int serverNettyPort;

    public static void main(String[] args) {
        // take port from args to start the client
        if (args.length == 4) {
            try {
                port = Integer.parseInt(args[0]);
                serverHostname = args[1];
                serverHTTPPort = Integer.parseInt(args[2]);
                serverNettyPort = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                log.error("Invalid arguments provided");
                System.exit(1);
            }
        } else {
            log.error("Please run the application with <port> <server hostname> <server http port> <server p2p port>");
            System.exit(1);
        }
        try {
            if ("localhost".equals(serverHostname)) {
                ClientData.baseURL = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + serverHTTPPort + "/user";
                ClientData.serverHostname = InetAddress.getLocalHost().getHostAddress();
            } else {
                ClientData.baseURL = "http://" + InetAddress.getByName(serverHostname).getHostAddress() + ":" + serverHTTPPort + "/user";
                ClientData.serverHostname = InetAddress.getByName(serverHostname).getHostAddress();
            }
        } catch (UnknownHostException e) {
            System.out.println("Server hostname not found");
            System.exit(1);
        }
        ClientData.httpServerPort = serverHTTPPort;
        ClientData.p2pServerPort = serverNettyPort;

        new SpringApplicationBuilder(CmdLineUIMain.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        String hostName = InetAddress.getLocalHost().getHostName();

        // test system ports if they are available for the application to start
        boolean nettyPort = PreConnectionTest.testPortAvailable(port);
        if (!nettyPort) {
            log.error("Please try to use another port to start the application");
            System.exit(1);
        }
        new ClientDriver(hostName, port);
    }
}