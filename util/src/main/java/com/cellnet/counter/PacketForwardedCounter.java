package com.cellnet.counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gui on 2017/1/9.
 * 该类用于统计每一个数据包在发送过程中的转发次数
 */
public class PacketForwardedCounter {
    private static Map<String, AtomicInteger> counters = new ConcurrentHashMap<String, AtomicInteger>();

    public static int countUp(String packetId, int value){
        if (null == counters.get(packetId)){
            counters.put(packetId, new AtomicInteger(1));
            return 1;
        }else{
            return counters.get(packetId).incrementAndGet();
        }
    }

    public static int getCount(){
        int count = 0;
        for (Map.Entry<String, AtomicInteger> entry: counters.entrySet()){
            count += entry.getValue().get();
        }
        return count;
    }

    public static double getAverageCount(){
        return  ((double)getCount())/counters.size();
    }
}
