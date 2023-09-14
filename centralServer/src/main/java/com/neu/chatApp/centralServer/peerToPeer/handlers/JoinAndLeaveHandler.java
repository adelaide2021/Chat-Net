package com.neu.chatApp.centralServer.peerToPeer.handlers;

import com.neu.chatApp.common.model.message.joinAndLeaveMessage.JoinAndLeaveMessage;
import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.common.interfaces.Handler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/*
在server端使用netty的意义何在？？？为什么要在server端又保存一分live nodes这种在client每个节点都保存的东西？？
 */
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
                // 为什么要加这一行，client端不是本来就有logout的API吗？？？
                //new RestTemplate().postForEntity("http://localhost:" + ServerData.myHttpPort + "/user/logout", msg.getNodeInfo().getId(), Void.class);
                OkHttpClient client = new OkHttpClient();

// 构建请求体
                String idAsString = String.valueOf(msg.getNodeInfo().getNodeId());
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), idAsString);

// 构建请求
                Request request = new Request.Builder()
                        .url("http://localhost:" + ServerData.myHttpPort + "/user/logout")
                        .post(requestBody)
                        .build();

                try {
                    // 执行HTTP请求
                    Response response = client.newCall(request).execute();

                    // 确保关闭响应体
                    if (response.body() != null) {
                        response.body().close();
                    }

                    // 关闭OkHttpClient
                    client.dispatcher().executorService().shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
