//package com.neu.chatApp.centralServer.service;
//
//import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
//import java.util.List;
//
//import com.neu.chatApp.common.interfaces.RMIServer;
//import com.neu.chatApp.centralServer.db.DB;
//import com.neu.chatApp.centralServer.db.MongoDB;
//import com.neu.chatApp.common.model.request.Request;
//import com.neu.chatApp.centralServer.db.User;
//import com.neu.chatApp.util.SimpleLogger;
//
//public class RMIServerImpl extends UnicastRemoteObject implements RMIServer {
//  private DB db;
//
//  public RMIServerImpl() throws RemoteException {
//    db = new MongoDB();
//  }
//
//  @Override
//  public boolean signUp(String username, String password) {
//    // find if username already exists
//    if (db.select(username) != null) {
//      SimpleLogger.error("Sign up failed: Username already exists");
//      return false;
//    }
//    db.insertUser(new User(username, password));
//    return true;
//  }
//
//  @Override
//  public boolean signIn(String username, String password) {
//    User user = db.select(username);
//    if (user != null && user.getPassword().equals(password)) {
//      SimpleLogger.info("Sign in success");
//      return true;
//    }
//    return false;
//  }
//
//  @Override
//  public void send(Request msg) {
//    System.out.println("Server received message: " + msg);
////    messages.add(msg);
//  }
//
//  @Override
//  public List<Request> get() {
////    return messages;
//    return null;
//  }
//}
