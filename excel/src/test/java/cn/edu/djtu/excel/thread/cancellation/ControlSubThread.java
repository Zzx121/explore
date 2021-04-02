package cn.edu.djtu.excel.thread.cancellation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zzx
 * @date 2021/3/16
 */
public class ControlSubThread implements Runnable {
    private int interval = 100;
    private Thread worker;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(true);

    public ControlSubThread(int interval) {
        this.interval = interval;
    }
    
    public void start() {
        worker = new Thread(this);
        worker.start();
    }
    
    public void stop() {
        running.set(false);
    }
    
    public void interrupt() {
//        running.set(false);
        worker.interrupt();
    }
    
    public boolean isRunning() {
        return running.get();
    }

    public AtomicBoolean isStopped() {
        return stopped;
    }
    

    @Override
    public void run() {
        running.set(true);
        stopped.set(false);
        while (running.get()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                running.set(false);
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, Failed to complete operation");
            }
            String threadName = Thread.currentThread().getName();
            if (threadName.equals("main")) {
                running.set(false);
            }
            System.out.println("Thread: " + threadName + "----- Task is executing!");
        }
        stopped.set(true);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ControlSubThread controlSubThread = new ControlSubThread(10000);
//        controlSubThread.start();
//        controlSubThread.start();
        controlSubThread.start();
//        controlSubThread.run();
//        controlSubThread.run();
//        controlSubThread.run();
        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        controlSubThread.stop();
        controlSubThread.interrupt();
        
        //Future
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        int factor = 100;
        Future<Integer> multiplierFuture = executorService.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted!");
            }
            return factor * factor;
        });

        System.out.println("Task canceled :" + multiplierFuture.isCancelled());
        while (!multiplierFuture.isDone()) {
            Thread.sleep(300);
            System.out.println("Calculating ....");
            multiplierFuture.cancel(true);
        }
        System.out.println("Task canceled :" + multiplierFuture.isCancelled());

        System.out.println(multiplierFuture.get());
    }
}
