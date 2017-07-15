package com.cellnet.domain;

import com.cellnet.base.AbstractNode;
import com.cellnet.enums.MsgType;
import com.cellnet.util.EnvUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by gui on 2017/1/7.
 */
public class Packet implements Delayed{
    //1.消息类型
    private MsgType msgType;

    //2.序列号
    private String seqNo;

    //3.消息源节点
    private AbstractNode source;

    //4.消息目的节点
    private AbstractNode target;

    //5.TTL
    private int TTL;

    //6.该消息已通过节点列表
    private ConcurrentLinkedQueue<AbstractNode> passedNodes;

    //7. 该消息以排除节点列表
    private Set<AbstractNode> extNodes;

    //8. 校验和
    private int checkSum;

    //9. 消息体
    private String data;

    private long triggeredTime;

    public Packet(MsgType msgType, String seqNo, AbstractNode source, AbstractNode target, int TTL,
                  ConcurrentLinkedQueue<AbstractNode> passedNodes, Set<AbstractNode> extNodes, int checkSum, String data) {
        this.msgType = msgType;
        this.seqNo = seqNo;
        this.source = source;
        this.target = target;
        this.TTL = TTL;
        this.passedNodes = new ConcurrentLinkedQueue<AbstractNode>(passedNodes);//不是指向同一个引用
        this.extNodes = new CopyOnWriteArraySet<AbstractNode>(extNodes);//不是指向同一个引用
        this.checkSum = checkSum;
        this.data = data;
    }

    public Packet(MsgType msgType, String seqNo, AbstractNode source, AbstractNode target, String data) {
        this.msgType = msgType;
        this.seqNo = seqNo;
        this.source = source;
        this.target = target;
        this.data = data;
        this.TTL = EnvUtil.getMaxPacketTTL();
        this.passedNodes = new ConcurrentLinkedQueue<AbstractNode>();
        this.extNodes = new CopyOnWriteArraySet<AbstractNode>();
        this.checkSum = this.hashCode();
    }

    public Packet setTTL(int TTL) {
        this.TTL = TTL;
        return this;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public AbstractNode getSource() {
        return source;
    }

    public AbstractNode getTarget() {
        return target;
    }

    public int getTTL() {
        return TTL;
    }

    public ConcurrentLinkedQueue<AbstractNode> getPassedNodes() {
        return passedNodes;
    }

    public Set<AbstractNode> getExtNodes() {
        return extNodes;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public String getData() {
        return data;
    }

    public Packet setPassedNodes(ConcurrentLinkedQueue<AbstractNode> passedNodes) {
        this.passedNodes = passedNodes;
        return this;
    }

    public Packet setExtNodes(Set<AbstractNode> extNodes) {
        this.extNodes = extNodes;
        return this;
    }

    public Packet setCheckSum(int checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    public Packet setDelayTime(long milliSeconds){
        this.triggeredTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(milliSeconds, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.triggeredTime - System.nanoTime(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        Packet otherMsg = null;
        if(o instanceof Packet){
            otherMsg = (Packet) o;
        }else{
            return 0;
        }

        if(this.triggeredTime>otherMsg.triggeredTime)
            return 1;
        else if(this.triggeredTime<otherMsg.triggeredTime)
            return -1;
        else
            return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        return this.seqNo.equals(packet.seqNo);

    }

    @Override
    public int hashCode() {
        return this.seqNo.hashCode();
    }

    /**
     * 克隆数据包，每经过一次转发，后续的处理都是针对不同的实体对象
     * @param packet
     * @return
     */
    public static Packet clone(Packet packet){
        Packet clonee = new Packet(packet.getMsgType(),
                                    packet.getSeqNo(),
                                    packet.getSource(),
                                    packet.getTarget(),
                                    packet.getTTL(),
                                    packet.getPassedNodes(),
                                    packet.getExtNodes(),
                                    packet.getCheckSum(),
                                    packet.getData()
                                    );
        return clonee;
    }
}
