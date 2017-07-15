package com.cellnet.counter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gui on 2017/1/11.
 */
public class PacketSentResultCounter {
    private static ConcurrentHashMap<String, AtomicBoolean> counters = new ConcurrentHashMap<String, AtomicBoolean>();

    public static int getSuccessSentCount(){
        int count = 0;
        for (ConcurrentHashMap.Entry<String, AtomicBoolean> entry : counters.entrySet()){
            if (entry.getValue().get()){
                count++;
            }
        }

        return count;
    }

    public static int getFailSentCount(){
        int count = 0;
        for (ConcurrentHashMap.Entry<String, AtomicBoolean> entry : counters.entrySet()){
            if (!entry.getValue().get()){
                count++;
            }
        }

        return count;
    }

    public static void initCounter(String id, boolean success){
        if (!counters.containsKey(id)){
            counters.put(id, new AtomicBoolean(success));
        }
    }

    public static void successCountup(String id){
        AtomicBoolean holder = counters.get(id);
        if (!holder.get()){
            while (!holder.compareAndSet(false, true))
                ;
        }
    }
}
