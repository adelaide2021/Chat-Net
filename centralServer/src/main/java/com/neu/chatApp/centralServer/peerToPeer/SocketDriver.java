package com.neu.chatApp.centralServer.peerToPeer;

import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;

/**
 *
 */
public class SocketDriver {
    public SocketDriver(int socketPort) {
        ServerData.init(socketPort);
    }
}
