package cn.edu.djtu.excel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zzx
 * @date 2020/12/30
 */
public class ConcurrencyTest {
    static final List<Integer> a = Collections.synchronizedList(new ArrayList<>());
    
    @Test
    void runTest() throws InterruptedException {
        int x = 20;
        Thread t = new Thread(() -> addIfAbsent(x));
        t.start();
        addIfAbsent(x);
        t.join();
        System.out.println(a);
    }
    
    private void addIfAbsent(int x) {
        synchronized (a) {
            if (!a.contains(x)) {
                a.add(x);
            }
        }
    }
    
    @Test
    void setTest() {
        Set<String> s1 = new HashSet<>();
        Set<String> s2 = new HashSet<>();
        List<String> l1 = Arrays.asList("z", "x", "y", "t", "a", "b");
        s1.add("a");
        s1.add("b");
        s1.add("c");
        s2.add("e");
        s2.add("c");
        s2.add("a");
        s2.addAll(s1);
        s1.addAll(l1);
//        boolean b = s1.retainAll(s2);
        
        System.out.println(s1);
        System.out.println(s2);
    }
    
    @Test
    void futureTest() throws InterruptedException, ExecutionException {
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
    
    @Test
    void completableFutureTest() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        CompletableFuture<Integer> computationCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted!");
            }
            return 10 * 20;
        });
        computationCF.cancel(true);
        System.out.println(computationCF.isCompletedExceptionally());
        System.out.println(computationCF.isCancelled());
        
        while (!computationCF.isDone()) {
            Thread.sleep(300);
            System.out.println("Task is executing!");
        }

        System.out.println(computationCF.get());
        
    }
    
    @Test
    void countDownLatchTest() throws InterruptedException {
        CountDownLatch doneLatch = new CountDownLatch(3);
        doneLatch.await();

        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            new Thread(() -> {
                processing("doneLatch sub tasks");
                doneLatch.countDown();
            }).start();
        }
    }

    @Test
    void workerDriverLatchTest() throws InterruptedException {
        CountDownLatch doneLatch = new CountDownLatch(3);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < 3; i++) {
            new Thread(new Worker(startLatch, doneLatch)).start();
        }
        processing("before let go");
        //let sub tasks execute
        startLatch.countDown();
        processing("after let go");
        //wait for all sub tasks to finish, without this, processing after will be not deterministic
        doneLatch.await();
        processing("waiting for all sub tasks to finish");
    }
    
    class Worker implements Runnable {
        CountDownLatch startLatch;
        CountDownLatch doneLatch;

        public Worker(CountDownLatch startLatch, CountDownLatch doneLatch) {
            this.startLatch = startLatch;
            this.doneLatch = doneLatch;
        }
        
        @Override
        public void run() {
            try {
                startLatch.await();
                processing("worker");
                doneLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        
    }
    private void processing(String scene) {
        System.out.println(Thread.currentThread().getName() + " is executing <" + scene + "> tasks ...");
    }
    
    @Test
    void streamGenerateTest() {
        Random random = new Random(20);
        List<Integer> integers = Stream.generate(() -> random.nextInt(1000)).limit(200).collect(Collectors.toList());
        System.out.println(integers.size());
    }
    
    private String appId = "wxf35854f0a0412a96";
    @Test
    void cacheBuilderTest() throws ExecutionException {
        LoadingCache<String, String> stringLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(120, TimeUnit.MINUTES).build(new CacheLoader<>() {
            @Override
            public String load(String key) throws Exception {
                Thread.sleep(500);
                return generateTokenMap().get(appId);
            }
        });
        System.out.println(stringLoadingCache.get(appId));
        System.out.println(stringLoadingCache.get(appId));
        System.out.println(stringLoadingCache.get(appId));
    }
    
    
    @Test
    void cacheBuilderFromTest() throws ExecutionException {
        LoadingCache<String, String> stringLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(120, TimeUnit.MINUTES).build(CacheLoader.from(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return generateTokenMap().get(appId);
        }));
        System.out.println(stringLoadingCache.get(appId));
        System.out.println(stringLoadingCache.get(appId));
        System.out.println(stringLoadingCache.get(appId));
    }
    
    private Map<String, String> generateTokenMap() {
        ConcurrentMap<String, String> tokenMap = Maps.newConcurrentMap();
        tokenMap.put(appId, "43_HY85FR9rgJBfdubHuqsDSYdUgO2k_" +
                "BP0VFlraYfZhRJ1zOrZeerz7oCZCi0vAHP5HlzA0O3ILGsxe4g4myQSmDCSqNIZ3D91_A6NskI62mLBJE3yDENTWRSm3ex2gsxCuHyMPtBwxzx7yx_0RSPeAAAMNK");
        
        return tokenMap;
    }
    
    @Test
    void httpClientTest() {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet httpGet = new HttpGet("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&" +
                    "appid=wxf35854f0a0412a96&secret=3d9f4e4c9ecc07364979b651bf1a48ab");
            try(CloseableHttpResponse httpResponse = client.execute(httpGet)) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    new JSONObject(EntityUtils.toString(entity)).getString("access_token");
                }
                EntityUtils.consume(entity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void getQRCodeTest() {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" +
                    "43_gnDgI3_VDC7cT7IIHBVG1yliF_YZI8KVps_vX57VH5nXfvNemaW5ElFxzgzz22qK8MnUGGEf57WNAVFVkJd8l3XvJUaugxvpWzoZDDX55joYbJW6lXeguxsYC4KNOM7-iIogWJVBXDzLFP1NNSMcACATYS");
            
            httpPost.setEntity(new StringEntity("{" +
                    " \"scene\":\"7c5ec298fa644373823e6f820157d745\"," +
                    " \"width\":230," +
                    " \"page\": \"pages/index/index\"" +
                    "}", ContentType.APPLICATION_JSON));

            try(CloseableHttpResponse httpResponse = client.execute(httpPost)) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    Path path = Paths.get("/tmp/images", "/qrcode");
                    Files.createDirectories(path);
                    Files.copy(entity.getContent(), path.resolve("qr01.jpeg"), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println(path.toFile());
                }
                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class ResultThread implements Runnable {
        private volatile int count;
        @Override
        public void run() {
            this.count = 2;
        }

        public int getCount() {
            return count;
        }
    }
    
    @Test
    void threadResultTest() throws InterruptedException {
        ResultThread resultThread = new ResultThread();
        Thread thread = new Thread(resultThread);
        thread.start();
        thread.join();
        System.out.println(resultThread.getCount());
    }
    
    @Test
    void threadResultCountDownLatch() throws InterruptedException {
        int[] intArray = new int[2];
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            intArray[0] = 2;
            latch.countDown();
        }).start();
        latch.await();
        System.out.println(intArray[0]);
    }
    
}
