package com.neu.chatApp.client.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/*
这个里面是调用http协议实现login/logout/
1、为什么要用http？？？http与peer to peer的关系到底是什么，为什么有些handler中还调用了http？？？
2.base url是从哪里传进来？本来是在client data中自带的
3、下面许多function需要修改，没有判断返回类型
 */
@Slf4j
public class RestClient {

  private final OkHttpClient httpClient;
  private String baseUrl;

  public RestClient(String baseUrl) {
    this.httpClient = new OkHttpClient();
    this.baseUrl = baseUrl;
  }

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

  public String signUp(String username, String password) {
    // 创建一个JSON对象
    String json = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/signup", jsonBody);
  }

  public String login(String username, String password) {
    // 创建一个JSON对象
    String json = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/login", jsonBody);
  }

  public String logout(String nodename) {
    // 创建一个JSON对象
    String json = "{\"username\": \"" + nodename + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/logout", jsonBody);
  }

  public String sendMessage(String username, String message) {
    // 创建一个JSON对象
    String json = "{\"username\": \"" + username + "\", \"message\": \"" + message + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/sendMessage", jsonBody);
  }

  public String getMessages() {
    // 创建一个空的JSON对象
    String json = "{}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    return sendPostRequest(baseUrl + "/getMessages", jsonBody);
  }

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