package cn.edu.djtu.excel;

import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImmutableTest {
    @Test
    void currentModifyString() {
        String str = "Hello";
        int threadCount = 6;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        for (int i = 0; i < threadCount; i++) {
            service.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("Executing Thread- " + Thread.currentThread().getName());
                String s = str + " World";
                System.out.println("Modified String " + s);
            });
        }
    }

    @Test
    void currentModifyStringBuilder() {
        StringBuilder str = new StringBuilder("Hello");
        int threadCount = 6;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        for (int i = 0; i < threadCount; i++) {
            service.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("Executing Thread- " + Thread.currentThread().getName());
                str.append(" World");
                System.out.println("Modified String " + str);
            });
        }
    }
}
