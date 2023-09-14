package com.neu.chatApp.common.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

// server端数据库中存储的数据就是user
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String password;
  private String salt;
  private String hostname;
  private int port;
  private boolean isOnline;

  public User(String username, String password, String salt) {
    this.username = username;
    this.password = password;
    this.salt = salt;
    this.isOnline = false;
  }
}
