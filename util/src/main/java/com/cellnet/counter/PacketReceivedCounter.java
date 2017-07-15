package com.cellnet.counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gui on 2017/1/9.
 * 该类用于统计目的节点对于同一个数据包收到的次数
 */
public class PacketReceivedCounter {
    private static Map<String, AtomicInteger> counters = new ConcurrentHashMap<String, AtomicInteger>();

    public static int countUp(String packetId, int value){
        if (null == counters.get(packetId)){
            counters.put(packetId, new AtomicInteger(1));
            return 1;
        }else{
            return counters.get(packetId).incrementAndGet();
        }
    }

    public static long getCount(){
        long count = 0L;
        for (Map.Entry<String, AtomicInteger> entry : counters.entrySet()){
            count += entry.getValue().get();
        }
        return count;
    }

    public static double getAverageCount(){
        return ((double)getCount())/counters.size();
    }

    public static int getSuccessSentCount(){
        return counters.size();
    }
}
