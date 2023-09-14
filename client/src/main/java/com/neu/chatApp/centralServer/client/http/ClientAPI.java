package com.neu.chatApp.centralServer.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
这个里面是调用http协议实现login/logout/
本类中只负责创建request，发送这个request并接收返回值
1、为什么要用http？？？http与peer to peer的关系到底是什么，为什么有些handler中还调用了http？？？
2、base url是从哪里传进来？本来是在client data中自带的
3、下面许多function需要修改，没有判断返回类型

 */
@Slf4j
public class ClientAPI {
  private final OkHttpClient httpClient;
  private String baseUrl;

  public ClientAPI(String baseUrl) {
    this.httpClient = new OkHttpClient();
    this.baseUrl = baseUrl;
  }

  // 辅助函数，帮助构建OKhttp中的请求
  private String sendPostRequest(String url, RequestBody body) {
    Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json") // 设置Content-Type为JSON
            .post(body)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.error("Unexpected code " + response);
        return null;
      }
      String result = response.body().string();
      log.info(result);
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 返回的应该是成功失败
  public String signUp(String username, String password) throws HttpClientErrorException, HttpServerErrorException, ResourceAccessException {
    String json = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/signup", jsonBody);
  }

  // 返回的是user这个object吧，前面的string是什么？？？
  public Map<String, Object> login(String username, String password, @NotNull String hostname, @NotNull int port) throws HttpClientErrorException, HttpServerErrorException, ResourceAccessException {
    Map<String, Object> body = new HashMap<>();
    body.put("username", username);
    body.put("password", password);
    body.put("hostname", hostname);
    body.put("port", port);

    ObjectMapper mapper = new ObjectMapper();
    String jsonBody;
    try {
      jsonBody = mapper.writeValueAsString(body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

    String responseJson = sendPostRequest("/login", requestBody);

    if (responseJson != null) {
      try {
        return mapper.readValue(responseJson, Map.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  // 返回成功与否
  public String logout(Long id) {
    // 创建一个JSON对象
    String json = "{\"userId\": " + id + "}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/logout", jsonBody);
  }

//  public String sendMessage(String username, String message) {
//    // 创建一个JSON对象
//    String json = "{\"username\": \"" + username + "\", \"message\": \"" + message + "\"}";
//    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));
//
//    return sendPostRequest(baseUrl + "/sendMessage", jsonBody);
//  }
//
//  public String getMessages() {
//    // 创建一个空的JSON对象
//    String json = "{}";
//    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));
//
//    return sendPostRequest(baseUrl + "/getMessages", jsonBody);
//  }

//  public static void main(String[] args) {
//    RestClient test = new RestClient("http://localhost:8080/api/user");
//    test.signUp("test-ok-http", "test");
//    test.login("test-ok-http", "test");
//    test.logout("test-ok-http");
//    test.signUp("test-ok-http3", "test");
//    test.sendMessage("test-ok-http3", "test message");
//    test.sendMessage("test-ok-http", "test message2");
//    // validate if user exists
//    test.getMessages();
//  }
}