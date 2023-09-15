package com.neu.chatApp.client.peerToPeer.services;

import com.neu.chatApp.common.model.message.Message;

/**
 * Send messages to other nodes provided by live node lists.
 * Can be used at ui for user send message to someone or broadcast.
 */
public interface Communication {

  /**
   * Send a message to the user with given id.
   *
   * @param id  the receiver id
   * @param msg the message
   */
  void send(Long id, Message msg);

  /**
   * Broadcast a message to all live nodes.
   *
   * @param msg message
   */
  void broadcast(Message msg);

//    /**
//     * Broadcast a message exclude the node with given id.
//     *
//     * @param msg the message to nodes
//     * @param id the id of excluded node
//     */
//    void broadcastExclude(TransmitProtocol msg, Long id);
}
