package com.cellnet.context;

import com.cellnet.base.AbstractNode;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by gui on 2017/1/9.
 */
public class NetworkContext {
    private static final Set<AbstractNode> endpoints = new CopyOnWriteArraySet<AbstractNode>();
    public static Set<AbstractNode> getEndpoints(){
        return endpoints;
    }

    public static void addEndpoint(AbstractNode node){
        endpoints.add(node);
    }
}
