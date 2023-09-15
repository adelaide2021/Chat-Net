package com.neu.chatApp.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.chatApp.client.peerToPeer.data.ClientData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles HTTP requests for signup/login/logout operations.
 * It is responsible for creating and sending requests, and receiving responses.
 */

@Slf4j
public class ClientAPI {
  private final OkHttpClient httpClient;

  public ClientAPI() {
    this.httpClient = new OkHttpClient();
  }

  // helper function to assist in constructing requests in OKhttp
  private Object sendPostRequest(String url, RequestBody body, Class<?> responseType) {
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

      String contentType = response.header("Content-Type");
      String result = response.body().string();
      log.info(result);

      if ("application/json".equals(contentType)) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          return objectMapper.readValue(result, responseType);
        } catch (IOException e) {
          throw new RuntimeException("Failed to parse JSON response", e);
        }
      } else if ("text/plain".equals(contentType)) {
        return result;
      } else {
        throw new RuntimeException("Unexpected content type: " + contentType);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String signUp(String username, String password) throws HttpClientErrorException, HttpServerErrorException, ResourceAccessException {
    String json = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    Object response = sendPostRequest(ClientData.baseURL + "/signup", jsonBody, String.class);
    return (response instanceof String) ? (String) response : null;
  }

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

    Object response = sendPostRequest(ClientData.baseURL + "/login", requestBody, Map.class);
    return (response instanceof Map) ? (Map<String, Object>) response : null;
  }

  public void logout(Long id) {
    // 创建一个JSON对象
    String json = "{\"userId\": " + id + "}";
    RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

    sendPostRequest(ClientData.baseURL + "/logout", jsonBody, Void.class);
  }
}