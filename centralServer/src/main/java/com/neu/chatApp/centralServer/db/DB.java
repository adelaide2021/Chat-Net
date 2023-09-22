package com.neu.chatApp.centralServer.db;

import com.neu.chatApp.common.model.request.Request;

import java.util.List;

public interface DB {
  void insertUser(User user);
  User select(String username);
  List<String> getOnlineUsers();
  void updateOnlineStatus(String username, boolean isOnline);
  void updateHostnameAndPort(String username, String hostname, int port);
  List<Request> getMessages();
  void insertMessage(Request msg);
}
