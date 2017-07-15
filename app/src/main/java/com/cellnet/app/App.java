package com.cellnet.app;

import com.cellnet.base.AbstractNode;
import com.cellnet.context.NetworkContext;
import com.cellnet.counter.ConnectionCounter;
import com.cellnet.counter.PacketForwardedCounter;
import com.cellnet.counter.PacketReceivedCounter;
import com.cellnet.counter.PacketSentResultCounter;
import com.cellnet.domain.Location;
import com.cellnet.domain.Packet;
import com.cellnet.enums.MsgType;
import com.cellnet.node.DRNodeImpl;
import com.cellnet.node.FMNodeImpl;
import com.cellnet.util.EnvUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.cellnet.util.EnvUtil.*;

/**
 * Created by gui on 2017/1/10.
 */
public class App {
    private static Logger logger = LogManager.getLogger("Cellnet");

    private static ScheduledExecutorService cacheScheduler = Executors.newScheduledThreadPool(getCacheWorkerNum());
    private static ScheduledExecutorService handshakeScheduler = Executors.newScheduledThreadPool(getHandshakeWorkerNum());
    private static ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(getHeartbeatWorkerNum());
    private static ScheduledExecutorService moveScheduler = Executors.newScheduledThreadPool(getMoveWorkerNum());
    private static ScheduledExecutorService packetScheduler = Executors.newScheduledThreadPool(getPacketWorkerNum());

    private static Object waiter = new Object();

    public static void main(String[] args) throws InterruptedException {
        buildNetwork();

        startNetwork();

        sendMsgWithEach();

        collectStatistics();
    }

    public static void buildNetwork(){
        int maxX = getMaxX();
        int maxY = getMaxY();
        int nodeCount = getNodeCount();

        String runMode = EnvUtil.getRunMode();

        AbstractNode newNode = null;
        for (Integer id=1; id<=nodeCount; id++ ){
            int x = (int)(Math.random()*maxX);
            int y = (int)(Math.random()*maxY);

            if ("DR".equals(runMode)){
                newNode = new DRNodeImpl(id, new Location(x, y));
            }else{
                newNode = new FMNodeImpl(id, new Location(x, y));
            }

            NetworkContext.addEndpoint(newNode);

        }
        for (AbstractNode node : NetworkContext.getEndpoints()){
            node.handshake();;
        }
        logger.info(String.format("初始化网络中所有节点，网络中共 %s 个节点", NetworkContext.getEndpoints().size()));
        logger.debug(String.format("网络中一共有 %s 条物理连接", ConnectionCounter.getCount()));
    }

    public static void startNetwork(){
        for (AbstractNode node : NetworkContext.getEndpoints()){
            cacheScheduler.scheduleAtFixedRate(node.getCacheTask(),0, 5, TimeUnit.MILLISECONDS);
            handshakeScheduler.scheduleAtFixedRate(node.getHandshakeTask(), 0, getHandshakeCycle(), TimeUnit.MILLISECONDS);
            heartbeatScheduler.scheduleAtFixedRate(node.getHeartbeatTask(), 0, getHeartbeatCycle(), TimeUnit.MILLISECONDS);
            moveScheduler.scheduleAtFixedRate(node.getMoveTask(), 5, getMoveCycle(), TimeUnit.MILLISECONDS);
            packetScheduler.scheduleAtFixedRate(node.getPacketTask(), 0, getPacketCycle(), TimeUnit.MILLISECONDS);
        }
    }

    public static void sendMsgWithEach(){
        logger.info("网络中每个节点开始相互发送一条消息");
        Packet packet= null;
        String seqNo = null;
        for (AbstractNode soure : NetworkContext.getEndpoints()){
            for (AbstractNode target : NetworkContext.getEndpoints()){
                if (!soure.equals(target)){
                    seqNo = soure.getId()+"-"+target.getId();
                    packet = new Packet(MsgType.SYN,
                            seqNo,
                            soure,
                            target,
                            "hello"
                            );

                    soure.sendMsg(packet);
                    PacketSentResultCounter.initCounter(seqNo, false);
                }
            }
        }
    }

    public static void collectStatistics() throws InterruptedException {
        outter:
        for (;;){
            //waiter.wait(5000);//使用wait()方法是为了让出CPU资源
            Thread.sleep(5000);
            //第一次挨个检查节点是否还有需要转发的数据包
            for (AbstractNode node : NetworkContext.getEndpoints()){
                if (node.haveMsgsToForward()){
                    continue outter;
                }
            }
            Thread.sleep(5000);
            //再次挨个检查节点是否还有需要转发的数据包
            for (AbstractNode node : NetworkContext.getEndpoints()){
                if (node.haveMsgsToForward()){
                    continue outter;
                }
            }
            cacheScheduler.shutdownNow();
            handshakeScheduler.shutdownNow();
            heartbeatScheduler.shutdownNow();
            moveScheduler.shutdownNow();
            packetScheduler.shutdownNow();

            break outter;
        }

        int nodeCount = getNodeCount();
        int msgSentCount = nodeCount*(nodeCount-1);

        int msgSuccessSentCount = PacketSentResultCounter.getSuccessSentCount();
        int msgFailSentCount = PacketSentResultCounter.getFailSentCount();

        long msgRecCount = PacketReceivedCounter.getCount();
        double msgRecAverage = PacketReceivedCounter.getAverageCount();

        long msgForwardedCount = PacketForwardedCounter.getCount();
        double msgForwardedAverage = PacketForwardedCounter.getAverageCount();

        logger.warn(String.format("total nodes in the network: %s", nodeCount));
        logger.warn(String.format("total connections count: %s", ConnectionCounter.getCount()));

        logger.warn(String.format("total sent packets count: %s", msgSentCount));
        logger.warn(String.format("successfully sent packets count: %s", msgSuccessSentCount));
        logger.warn(String.format("failed sent packets count: %s", msgFailSentCount));
        logger.warn(String.format("successfully sent packets rate: %s", (double)msgSuccessSentCount/msgSentCount));

        logger.warn(String.format("all nodes received total packets %s", msgRecCount));
        logger.warn(String.format("each packet is received averagely: %s", msgRecAverage));

        logger.warn(String.format("total forward actions: %s", msgForwardedCount));
        logger.warn(String.format("each packet is forwarded averagely: %s", msgForwardedAverage));

    }
}
