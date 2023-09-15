package com.neu.chatApp.centralServer.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.common.interfaces.Handler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class JoinAndLeaveHandler implements Handler<JoinAndLeaveMessage> {
    public JoinAndLeaveHandler() {}

    @Override
    public void handle(JoinAndLeaveMessage msg, ChannelHandlerContext ctx) {
        switch (msg.getSubType()) {
            case JOIN:
                ServerData.serverLiveNodes.add(msg.getNodeInfo());
                log.info("A new node joined: " + msg.getNodeInfo());
                break;
            case LEAVE:
                ServerData.serverLiveNodes.remove(msg.getNodeInfo().getNodeId());
                log.info("A node left id: " + msg.getNodeInfo().getNode().getNodeId());
                // logout the node
                OkHttpClient client = new OkHttpClient();
                String idAsString = String.valueOf(msg.getNodeInfo().getNodeId());
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), idAsString);
                Request request = new Request.Builder()
                        .url("http://localhost:" + ServerData.myHttpPort + "/user/logout")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.body() != null) {
                        response.body().close();
                    }
                    client.dispatcher().executorService().shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
