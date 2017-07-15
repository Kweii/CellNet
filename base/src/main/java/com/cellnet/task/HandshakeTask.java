package com.cellnet.task;

import com.cellnet.base.AbstractNode;

/**
 * Created by gui on 2017/1/12.
 */
public class HandshakeTask implements Runnable {
    private AbstractNode node;

    public HandshakeTask(AbstractNode node){
        this.node = node;
    }

    @Override
    public void run() {
        node.handshake();
    }
}
