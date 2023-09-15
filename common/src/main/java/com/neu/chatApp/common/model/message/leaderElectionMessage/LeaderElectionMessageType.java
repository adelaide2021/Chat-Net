package com.neu.chatApp.common.model.message.leaderElectionMessage;


public enum LeaderElectionMessageType {


    // Server to Client
    // Used when the leader disappears or crashes, the server requests a node to start the leader election process.
    // Contains: type + subtype
    SERVER_REQUEST,

    // Server to Client
    // If a node is the first to join the p2p network, the server authorizes that node as the leader.
    // Contains: type + subtype + leaderToken
    SERVER_AUTH,

    // Client(Leader) to Client
    // The current "leader" node requests the start of the leader election process.
    // Contains: type + subtype
    LEADER_REQUEST,

    // Client to Client
    // Nodes report their status to the initiating node during leader election.
    // Contains: type + subtype + nodeInfo + performanceWeight
    NODE_REPORT,

    // Client(Leader) to Server
    // Leader node leaves the p2p group and returns the leader token.
    // Contains: type + subtype + nodeInfo + leaderToken
    TOKEN_RETURN,

    // Client(Leader) to Server
    // The node reports the result of the leader election to the leader.
    // Contains: type + subtype + nodeInfo
    CLIENT_REPORT,

    // Client(Leader) to Client
    // Leader declares the node as the new Leader.
    // Contains: type + subtype + nodeInfo
    LEADER_CHOSEN
}
