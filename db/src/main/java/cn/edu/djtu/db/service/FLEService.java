package cn.edu.djtu.db.service;

import cn.edu.djtu.db.entity.fle.GlobalConfigs;
import cn.edu.djtu.db.entity.fle.Node;
import cn.edu.djtu.db.entity.fle.Payload;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @ClassName FLEService
 * @Description: TODO
 * @Author zzx
 * @Date 2023/2/18
 **/
public class FLEService {
    /**
     * The node queue just simply dealing here
     * Every node subscribe the payload queue and also send messages to that queue, just simulate teh process of
     * the dealing process through multiple Threads
     */
    public FLEService() {
        if (GlobalConfigs.allNodes == null) {
            GlobalConfigs.allNodes = new ConcurrentSkipListSet<>();
        }
        if (GlobalConfigs.allNodes.size() == 0) {
            GlobalConfigs.allNodes.add(Node.builder().id(1).build());
            GlobalConfigs.allNodes.add(Node.builder().id(2).build());
            GlobalConfigs.allNodes.add(Node.builder().id(3).build());
            GlobalConfigs.allNodes.add(Node.builder().id(4).build());
            GlobalConfigs.allNodes.add(Node.builder().id(5).build());
        }
    }

    public void nodeDealing(Payload payload) throws InterruptedException {
        long currentId = payload.getId();
        //Put current payload to the shared payloads list
        GlobalConfigs.allNodes.stream().map(Node::getId).forEach(id -> {
            if (id != currentId) {
                ConcurrentLinkedQueue<Payload> payloads = GlobalConfigs.cachedPayloads.get(Node.builder().id(id).build());
                if (payloads != null) {
                    System.out.println("Adding payload to queue: " + payload);
                    payloads.add(payload);
                } else {
                    payloads = new ConcurrentLinkedQueue<>();
                    System.out.println("Adding payload to queue: " + payload);
                    payloads.add(payload);
                    GlobalConfigs.cachedPayloads.put(Node.builder().id(id).build(), payloads);
                }
            }
        });

        // Consume next dealing payload
        ConcurrentLinkedQueue<Payload> payloads = GlobalConfigs.cachedPayloads.get(Node.builder().id(currentId).build());
        if (payloads != null && payloads.size() > 0) {
            Payload dealingOne = payloads.poll();
            System.out.println("Dealing payload ---" + dealingOne);
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) {
        Payload first = Payload.builder().id(1).round(1).vote(Payload.Vote.builder().lastZxid("First").id(1).build()).state(Payload.State.ELECTION)
                .build();
        Payload second = Payload.builder().id(2).round(1).vote(Payload.Vote.builder().lastZxid("Second").id(2).build()).state(Payload.State.ELECTION)
                .build();
        Payload third = Payload.builder().id(3).round(1).vote(Payload.Vote.builder().lastZxid("Third").id(3).build()).state(Payload.State.ELECTION)
                .build();
        Payload fourth = Payload.builder().id(4).round(1).vote(Payload.Vote.builder().lastZxid("Fourth").id(4).build()).state(Payload.State.ELECTION)
                .build();
        Payload fifth = Payload.builder().id(5).round(1).vote(Payload.Vote.builder().lastZxid("Fifth").id(5).build()).state(Payload.State.ELECTION)
                .build();

        Thread t = new Thread(() -> {
            try {
                new FLEService().nodeDealing(first);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        Thread t2 = new Thread(() -> {
            try {
                new FLEService().nodeDealing(second);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t2.start();

        Thread t3 = new Thread(() -> {
            try {
                new FLEService().nodeDealing(third);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t3.start();
    }
}
