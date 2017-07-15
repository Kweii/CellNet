package com.cellnet.node;

import com.cellnet.base.AbstractNode;
import com.cellnet.context.NetworkContext;
import com.cellnet.counter.ConnectionCounter;
import com.cellnet.domain.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by gui on 2017/1/12.
 */
public class FMNodeImpl extends AbstractNode {
    private int id;
    //key表示分享节点的来源节点，value集合表示所分享的节点集
    private ConcurrentHashMap<AbstractNode, CopyOnWriteArraySet<AbstractNode>> sharedNodes;

    Logger logger = null;

    public FMNodeImpl(int id, Location location){
        super(id, location);
        this.id = id;
        this.sharedNodes = new ConcurrentHashMap<AbstractNode, CopyOnWriteArraySet<AbstractNode>>();
        logger = LogManager.getLogger("Endpoint-"+id);
    }


    @Override
    public void handshake() {
        if (needHandshake()){
            for (AbstractNode node : NetworkContext.getEndpoints()){
                if (!this.withinSharedNodes(node) && !((FMNodeImpl)node).withinSharedNodes(this)){
                    if (this.withinDirectCommuRange(node) && !this.isNeighbor(node) && !this.equals(node)){
                        this.addNeighbor(node);
                        node.addNeighbor(this);
                        ConnectionCounter.countUp();
                        //分享邻居节点表
                        sharedNodes.put(node, new CopyOnWriteArraySet<AbstractNode>(node.getNeighbors()));
                        ((FMNodeImpl)node).sharedNodes.put(this, new CopyOnWriteArraySet<AbstractNode>(this.getNeighbors()));
                        //logger.info(String.format("connect to node:%s", node.getId()));
                    }
                }
            }
        }
    }

    @Override
    public void heartbeat() {
        if (needHeatbeat()){
            for (AbstractNode neighbor : this.getNeighbors()){
                if (!this.withinDirectCommuRange(neighbor)){
                    logger.fatal(String.format("disconnect with node:%s",  neighbor.getId()));
                    this.getNeighbors().remove(neighbor);
                    neighbor.getNeighbors().remove(this);
                    //从分享节点表中删除相应的节点
                    sharedNodes.remove(neighbor);
                    ((FMNodeImpl)neighbor).sharedNodes.remove(this);

                    //立即调用handshake
                    this.handshake();
                    neighbor.handshake();

                    ConnectionCounter.countDown();
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        FMNodeImpl fmNode = (FMNodeImpl) o;

        return id == fmNode.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean withinSharedNodes(AbstractNode node){
        for (ConcurrentHashMap.Entry<AbstractNode, CopyOnWriteArraySet<AbstractNode>> entry : sharedNodes.entrySet()){
            if (entry.getValue().contains(node)){
                return true;
            }
        }

        return false;
    }
}
