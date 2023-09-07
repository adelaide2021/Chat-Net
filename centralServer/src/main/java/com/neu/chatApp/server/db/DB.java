package com.neu.chatApp.server.db;

import com.neu.chatApp.model.request.Request;
import com.neu.chatApp.model.user.User;

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
