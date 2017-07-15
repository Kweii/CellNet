package com.cellnet.node;

import com.cellnet.base.AbstractNode;
import com.cellnet.context.NetworkContext;
import com.cellnet.counter.ConnectionCounter;
import com.cellnet.domain.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gui on 2017/1/9.
 */
public class DRNodeImpl extends AbstractNode {
    private Logger logger = null;

    public DRNodeImpl(int id, Location location){
        super(id, location);
        logger = LogManager.getLogger("Endpoint-"+id);
    }

    @Override
    public void handshake() {
        if (needHandshake()){
            int count = 0;
            for (AbstractNode node : NetworkContext.getEndpoints()){
                if (this.withinDirectCommuRange(node) && !this.isNeighbor(node) && !this.equals(node)){
                    this.addNeighbor(node);
                    node.addNeighbor(this);
                    ConnectionCounter.countUp();
                    count++;
                    if (count==5){
                        logger.info(String.format("connect to node:%s", node.getId()));
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
                    ConnectionCounter.countDown();

                    this.handshake();
                    neighbor.handshake();
                }
            }
        }
    }

}
