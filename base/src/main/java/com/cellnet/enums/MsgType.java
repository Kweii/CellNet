package com.cellnet.enums;

/**
 * Created by gui on 2017/1/7.
 */
public enum MsgType {
    SYN ,   //握手探测消息
    ACK1,   //探测确认消息
    ACK2,   //探测再确认消息
    HB, //心跳消息
    HB_ACK, //心跳确认消息
    DATA,   //用用消息
    DATA_ACK    //应用确认消息
}
