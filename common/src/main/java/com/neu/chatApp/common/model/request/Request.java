package com.neu.chatApp.common.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Request {
  String username;
  String content;

  public Request(String username, String text) {
    this.username = username;
    this.content = text;
  }

  @Override
  public String toString() {
    return username + ": " + content;
  }
}
