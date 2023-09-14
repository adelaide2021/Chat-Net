package com.neu.chatApp.common.model.message;

/**
 * 每个模块处理程序的通用类型，用于标识其任务。由此模块向下分发具体的事务
 * 事务类型包括：选主 + 传输 + JOIN and LEAVE + Transaction业务
 */
public enum MessageType {
    JOIN_AND_LEAVE,

    TRANSACTION,

    LEADER_ELECTION,

    COMMUNICATION
}
