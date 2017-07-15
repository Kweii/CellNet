package com.cellnet.counter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gui on 2017/1/9.
 */
public class ConnectionCounter {
    private static AtomicInteger connectionCounter = new AtomicInteger();
    public static int countUp(){
        return connectionCounter.incrementAndGet();
    }
    public static int countDown(){
        return connectionCounter.decrementAndGet();
    }

    public static int getCount(){
        return connectionCounter.get();
    }
}
