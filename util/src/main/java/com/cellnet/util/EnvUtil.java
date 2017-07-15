package com.cellnet.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by gui on 2017/1/9.
 */
public class EnvUtil {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(EnvUtil.class.getResourceAsStream("/environment.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取底层物理直接传输距离
     * @return
     */
    public static int getCommuDistance(){
        Integer value = null;
        String valueStr = properties.getProperty("BLUETOOTH_COMMU_DISTANCE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 20;
        }
        return value;
    }

    public static int getMaxPacketTTL(){
        Integer value = null;
        String valueStr = properties.getProperty("MAX_PACKET_TTL");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 25;
        }
        return value;
    }

    public static int getMaxX(){
        Integer value = null;
        String valueStr = properties.getProperty("MAX_X");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 100;
        }
        return value;
    }

    public static int getMaxY(){
        Integer value = null;
        String valueStr = properties.getProperty("MAX_Y");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 100;
        }
        return value;
    }

    public static int getNodeCount(){
        Integer value = null;
        String valueStr = properties.getProperty("ENDPOINT_COUNT");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 100;
        }
        return value;
    }

    public static long getMsgCacheTime(){
        Long value = null;
        String valueStr = properties.getProperty("CACHE_TIME");
        try {
            value = Long.valueOf(valueStr);
        }catch (Exception e){
            value = 1000L*60*5;
        }

        return value;
    }

    public static int getPacketCycle(){
        Integer value = null;
        String valueStr = properties.getProperty("PACKET_CYCLE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 50;
        }
        return value;
    }

    public static int getCacheWorkerNum(){
        Integer value = null;
        String valueStr = properties.getProperty("CACHE_WORKER");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 1;
        }
        return value;
    }

    public static int getHandshakeWorkerNum(){
        Integer value = null;
        String valueStr = properties.getProperty("HANDSHAKE_WORKER");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 5;
        }
        return value;
    }

    public static int getHeartbeatWorkerNum(){
        Integer value = null;
        String valueStr = properties.getProperty("HEARTBEAT_WORK");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 5;
        }
        return value;
    }

    public static int getMoveWorkerNum(){
        Integer value = null;
        String valueStr = properties.getProperty("MOVE_WORKER");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 5;
        }
        return value;
    }

    public static int getPacketWorkerNum(){
        Integer value = null;
        String valueStr = properties.getProperty("PACKET_WORKER");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 10;
        }
        return value;
    }

    public static int getNodeMoveSpace(){
        Integer value = null;
        String valueStr = properties.getProperty("NODE_MOVE_SPACE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 2;
        }
        return value;
    }

    public static int getMoveCycle(){
        Integer value = null;
        String valueStr = properties.getProperty("MOVE_CYCLE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 2;
        }
        return value;
    }
    public static int getHandshakeCycle(){
        Integer value = null;
        String valueStr = properties.getProperty("HANDSHAKE_CYCLE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 2;
        }
        return value;
    }
    public static int getHeartbeatCycle(){
        Integer value = null;
        String valueStr = properties.getProperty("HEARTBEAT_CYCLE");
        try {
            value = Integer.valueOf(valueStr);
        }catch (Exception e){
            value = 2;
        }
        return value;
    }

    public static String getRunMode(){
        return properties.getProperty("RUN_MODE");
    }


}
