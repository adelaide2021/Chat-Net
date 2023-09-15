package com.neu.chatApp.gui;


import com.neu.chatApp.client.peerToPeer.data.ClientData;

public class ClientDriver {

    public ClientDriver(String hostname, int port) {
        ClientData.init(hostname, port);
        new Thread(new CmdLineUI()).start();
    }
}
