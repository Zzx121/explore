package cn.edu.djtu.db.entity.fle;

import java.util.concurrent.*;

/**
 * @ClassName GlobalConfigs
 * @Description: Store messages like nodes information and cached payloads to be consumed by each node
 * @Author zzx
 * @Date 2023/2/8
 **/
public class GlobalConfigs {
    public static ConcurrentHashMap<Node, ConcurrentLinkedQueue<Payload>> cachedPayloads = new ConcurrentHashMap<>();
    public static ConcurrentSkipListSet<Node> allNodes = new ConcurrentSkipListSet<>();

}
