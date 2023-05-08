package com.neu.chatApp.client.gui;

import com.neu.chatApp.api.RMIServer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class Chat {
  JPanel panelMain;
  JList chatHistory;
  JTextPane newMessage;
  JButton sendButton;
  JLabel currentUser;
  JButton logoutButton;
  RMIServer server;
  final List<String> messageHistory = new ArrayList<>();

  public Chat() {
//    try {
//      Registry registry = LocateRegistry.getRegistry("localhost", 6666);
//      server = (RMIServer) registry.lookup("api.Server");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Chat");
    frame.setContentPane(new Chat().panelMain);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
