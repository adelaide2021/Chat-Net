package com.neu.chatApp.client.rest;

import com.neu.chatApp.logger.SimpleLogger;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {

  // one instance, reuse
  private final OkHttpClient httpClient;
  private String baseUrl;

  public RestClient(String baseUrl) {
    this.httpClient = new OkHttpClient();
    this.baseUrl = baseUrl;
  }

  private String sendPostRequest(String url, RequestBody body) {
    Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "OkHttp Bot")
            .post(body)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        SimpleLogger.error("Unexpected code " + response);
        return null;
      }
      String result = response.body().string();
      SimpleLogger.info(result);
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String signUp(String username, String password) {
    RequestBody formBody = new FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build();

    return sendPostRequest(baseUrl + "/signup", formBody);
  }

  public String login(String username, String password) {
    RequestBody formBody = new FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build();

    return sendPostRequest(baseUrl + "/login", formBody);
  }


  public String logout(String username) {
    RequestBody formBody = new FormBody.Builder()
            .add("username", username)
            .build();

    return sendPostRequest(baseUrl + "/logout", formBody);
  }

  public String sendMessage(String username, String message) {
    // TODO: changed formBody to jsonBody
    RequestBody formBody = new FormBody.Builder()
            .add("username", username)
            .add("message", message)
            .build();

    return sendPostRequest(baseUrl + "/sendMessage", formBody);
  }

  public String getMessages() {
    RequestBody formBody = new FormBody.Builder()
            .build();

    return sendPostRequest(baseUrl + "/getMessages", formBody);
  }

  public static void main(String[] args) {
    RestClient test = new RestClient("http://localhost:8080/api/user");
    test.signUp("test-ok-http", "test");
    test.login("test-ok-http", "test");
    test.logout("test-ok-http");
    test.signUp("test-ok-http3", "test");
    test.sendMessage("test-ok-http3", "test message");
    test.sendMessage("test-ok-http", "test message2");
    // validate if user exists
    test.getMessages();
  }
}