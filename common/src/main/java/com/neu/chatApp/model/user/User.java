package com.neu.chatApp.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

  public User(String username, String email, String password) {
    this.username = username;
    this.password = password;
    this.isOnline = false;
  }

  public User(String username, String password) {
    this(username, "", password);
  }
}
