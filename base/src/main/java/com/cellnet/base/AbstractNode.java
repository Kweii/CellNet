package com.cellnet.base;

import com.cellnet.counter.PacketForwardedCounter;
import com.cellnet.counter.PacketReceivedCounter;
import com.cellnet.counter.PacketSentResultCounter;
import com.cellnet.domain.Location;
import com.cellnet.domain.Packet;
import com.cellnet.task.*;
import com.cellnet.util.EnvUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;

/**
 * Created by gui on 2017/1/7.
 */
public abstract class AbstractNode{
    /**
     * 节点ID
     */
    private int id;
    /**
     * 节点位置
     */
    private Location location;
    /**
     * 将定期清理缓存任务提取出来
     */
    private CacheTask cacheTask;
    /**
     * 将握手机制提取出来
     */
    private HandshakeTask handshakeTask;
    /**
     * 将心跳机制提取出来
     */
    private HeartbeatTask heartbeatTask;
    /**
     * 将随机移动模拟任务提取出来
     */
    private MoveTask moveTask;
    /**
     * 将数据包处理任务提取出来
     */
    private PacketTask packetTask;
    /**
     * 邻居节点表
     */
    private Set<AbstractNode> neighborNodes = new CopyOnWriteArraySet<AbstractNode>();
    /*
	 * 存放待转发的所有消息
	 */
    private ConcurrentLinkedQueue<Packet> toForwardMsgs = new ConcurrentLinkedQueue<Packet>();
    /**
     * 存放所有近期N秒内已经转发的消息
     */
    private DelayQueue<Packet> forwardedMsgs = new DelayQueue<Packet>();
    /**
     * 节点上一次执行心跳机制时间（毫秒）
     */
    private long lastHeartbeatTime;
    /**
     * 节点上一次握手机制的时间（毫秒）
     */
    private long lastHandshakeTime;
    /**
     * 节点上一次移动的时间（毫秒）
     */
    private long lastMovedTime;

    private Logger logger = null;

    public AbstractNode(){
    }

    public AbstractNode(int id, Location location) {
        this.id = id;
        this.location = location;

        this.cacheTask = new CacheTask(this);
        this.handshakeTask = new HandshakeTask(this);
        this.heartbeatTask = new HeartbeatTask(this);
        this.moveTask = new MoveTask(this);
        this.packetTask = new PacketTask(this);

        lastMovedTime = System.currentTimeMillis();
        logger = LogManager.getLogger("Endpoint-"+id);
    }

    /**
     * handshake机制，检查当前节点与附近节点是否可以建立直接的物理连接
     * @return
     */
    public abstract void handshake();

    /**
     * heartbeat机制，检查当前节点邻居节点表中某个节点与当前节点是否处于正常的物理连接状态
     * @return
     */
    public abstract void heartbeat();

    /**
     * 转发所有待转发数据包（在转发过程中完成数据包的接收）
     * @return
     */
    public void forwardMsgs(){
        Packet packet = null;
        //startTimestamp是为了防止一个任务太久时间占用CPU
        //long startTimestamp = System.currentTimeMillis();
        while(null != (packet = toForwardMsgs.poll())){
            //检查TTL以决定是否执行消息转发
            int TTL = packet.getTTL();
            if (TTL>=1){
                packet.setTTL(TTL-1);
                logger.debug(String.format("forward Packet(seqNo=%s  data=%s  TTL=%s)", packet.getSeqNo(), packet.getData(), TTL));

                Packet clonee = null;
                for (AbstractNode neighbor : getNeighbors()){
                    //取消对this.withinDirectCommuRange(neighbor)检查更合理
                    if (!packet.getExtNodes().contains(neighbor)){
                        //克隆出新的数据包实体(每实际转发一次，产生一个新的数据包实体)
                        clonee = Packet.clone(packet);
                        //将当前节点的邻居节点全部加入克隆体的以排除集合中
                        clonee.getExtNodes().addAll(this.getNeighbors());
                        clonee.getPassedNodes().offer(this);
                        //模拟执行实际的转发操作
                        neighbor.receiveMsg(clonee);
                        //记录此次转发数量
                        PacketForwardedCounter.countUp(packet.getSeqNo(), 1);
                    }
                }
            }
        }

        //logger.fatal(System.currentTimeMillis()-startTimestamp+"");
    }

    /**
     * 节点接收消息，并将收到的消息放进自己的“待转发消息列表中”，
     * 该方法是在forwardMsg(Packet packet)方法中对其每个邻近节点调用
     * @param packet
     */
    public void receiveMsg(Packet packet){
        //根据消息的目的地判断是否为当前节点，如果是则不进行消息转发
        if (packet.getTarget().equals(this)){
            String seqNo = packet.getSeqNo();
            PacketReceivedCounter.countUp(seqNo, 1);
            PacketSentResultCounter.successCountup(seqNo);
            logger.debug(String.format("receive Packet (seqNo=%s  data=%s  TTL=%s)", seqNo, packet.getData(), packet.getTTL()));

        }else{
            // 如果当前节点的缓存中不存在该消息，才执行消息接收操作,准备转发
            if (!forwardedMsgs.contains(packet)){
                if (packet.getTTL()>=1){
                    //将当前消息缓存到当前节点，并准备转发
                    this.cacheMsg(packet);
                    toForwardMsgs.offer(packet);
                }
            }
        }
    }

    /**
     * 从当前节点开始发送消息，直接将消息放到待发送消息队列中
     * @param packet
     */
    public void sendMsg(Packet packet){
        toForwardMsgs.offer(packet);
        this.cacheMsg(packet);
    }

    /**
     * 将当前收到的消息缓存到节点本地，该方法是在receiveMsg(Packet packet)方法中调用
     * @param packet
     */
    public void cacheMsg(Packet packet){
        packet.setDelayTime(EnvUtil.getMsgCacheTime());
        forwardedMsgs.add(packet);
    }

    /**
     * 清空当前节点已过时消息
     * @return
     */
    public int clearTimeoutCache(){
        int count = 0;
        Packet packet = null;
        while((packet=forwardedMsgs.poll()) != null && count%3==0){
            logger.debug(String.format("clear timeout packet: %s", packet.getSeqNo()));
            count++;
        }
        return count;
    }

    /**
     * 获得当前节点的所有邻居节点
     * @return
     */
    public Set<AbstractNode> getNeighbors(){
        return neighborNodes;
    }

    /**
     * 把一个节点添加到当前节点的邻居节点表中
     * @param neighbor
     * @return
     */
    public boolean addNeighbor(AbstractNode neighbor){
        return neighborNodes.add(neighbor);
    }

    /**
     * 判断一个节点是否是当前节点的“邻居节点”
     * @param node
     * @return
     */
    public boolean isNeighbor(AbstractNode node){
        return neighborNodes.contains(node);
    }

    /**
     * 判断一个节点是否在当前节点的直接通信范围内
     * @param node
     * @return
     */
    public boolean withinDirectCommuRange(AbstractNode node){
        int x = this.location.getX();
        int y = this.location.getY();

        int xx = node.getLocation().getX();
        int yy = node.getLocation().getY();

        return Math.pow(EnvUtil.getCommuDistance(), 2) > (Math.pow((xx-x), 2)+Math.pow((yy-y), 2));
    }

    /**
     * 模拟在实际应用场景中各个节点无规则的移动
     * @return
     */
    public Location randomMove(){
        //至少五秒钟以上运动一次
        //NetworkContext.getEndpoints();
        if (needMove()){
            int moveSpace = EnvUtil.getNodeMoveSpace();
            int x = location.getX();
            int y = location.getY();
            if (x<=0){
                x = x + moveSpace;
            }else if (x>= EnvUtil.getMaxX()){
                x = x - moveSpace;
            }else{
                x = x + (Math.random()>0.5 ? -moveSpace : moveSpace);
            }

            if (y<=0){
                y = y+moveSpace;
            }else if (y>= EnvUtil.getMaxY()){
                y = y-moveSpace;
            }else{
                y = y + (Math.random()>0.5 ? -moveSpace : moveSpace);
            }
            logger.debug(String.format("move from (%s, %s) to (%s, %s)",
                    location.getX(), location.getY(), x, y));

            this.location.setX(x);
            this.location.setY(y);
        }
        return location;
    }

    /**
     * 判断当前节点是否需要重新移动位置
     * @return
     */
    public boolean needMove(){
        boolean needMove = System.currentTimeMillis()-lastMovedTime>=EnvUtil.getMoveCycle();
        if (needMove){
            lastMovedTime = System.currentTimeMillis();
        }
        return needMove;
    }

    /**
     * 判断当前节点是否需要执行心跳机制
     * @return
     */
    public boolean needHeatbeat(){
        boolean needHeartbeat = System.currentTimeMillis()-lastHeartbeatTime>=EnvUtil.getHeartbeatCycle();
        if (needHeartbeat){
            lastHeartbeatTime = System.currentTimeMillis();
        }

        return needHeartbeat;
    }

    /**
     * 判断当前节点是否需要执行握手机制
     * @return
     */
    public boolean needHandshake(){
        boolean needHandshke = System.currentTimeMillis() - lastHandshakeTime>EnvUtil.getHandshakeCycle();
        if (needHandshke){
            lastHandshakeTime = System.currentTimeMillis();
        }

        return needHandshke;
    }

    public boolean haveMsgsToForward(){
        return !this.toForwardMsgs.isEmpty();
    }

    /**
     * 获得当前节点的location
     * @return
     */
    public Location getLocation(){
        return location;
    }

    public int getId(){
        return id;
    }

    public CacheTask getCacheTask() {
        return cacheTask;
    }

    public HandshakeTask getHandshakeTask() {
        return handshakeTask;
    }

    public HeartbeatTask getHeartbeatTask() {
        return heartbeatTask;
    }

    public MoveTask getMoveTask() {
        return moveTask;
    }

    public PacketTask getPacketTask() {
        return packetTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractNode that = (AbstractNode) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}

