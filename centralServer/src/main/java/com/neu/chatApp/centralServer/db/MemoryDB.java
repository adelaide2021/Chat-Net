package com.neu.chatApp.centralServer.db;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemoryDB {
  public List<String> onlineUsers;

  public MemoryDB(DB db) {
    this.onlineUsers = db.getOnlineUsers();
    // TODO: try to ping each online user and update online status

  }
}
