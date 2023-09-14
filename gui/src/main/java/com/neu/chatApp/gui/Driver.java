package com.neu.chatApp.gui;


import com.neu.chatApp.centralServer.client.peerToPeer.data.ClientData;

public class Driver {

    public Driver(String hostname, int port) {
        ClientData.init(hostname, port);
        new Thread(new CmdLineUI()).start();
    }
}
