//package com.neu.chatApp.common.interfaces;
//
//import com.neu.chatApp.common.model.request.Request;
//
//import java.rmi.Remote;
//import java.rmi.RemoteException;
//import java.util.List;
//
//public interface RMIServer extends Remote {
//  boolean signUp(String username, String password) throws RemoteException;
//  boolean signIn(String username, String password) throws RemoteException;
//  void send(Request msg) throws RemoteException;
//  List<Request> get() throws RemoteException;
//}
