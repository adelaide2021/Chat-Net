package com.neu.chatApp.centralServer.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * The data stored in the server-side database represents user information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_info")
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
