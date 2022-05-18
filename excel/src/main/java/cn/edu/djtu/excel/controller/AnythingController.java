package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.common.exception.PhoneAlreadyUsedException;
import cn.edu.djtu.excel.util.basic.RequestUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author zzx
 * @date 2020/4/16
 **/
@Log
@RestController
@RequestMapping("/anything")
public class AnythingController {
    
    @PostMapping("/tokenFilter")
    public void tokenFilterPost(HttpServletRequest request, @RequestParam String token) {
        log.info("json body third time: " + RequestUtil.getRequestParamValue(request, "token"));
//        throw new PhoneAlreadyUsedException();
    }
    
    @PostMapping("/tokenFilterJson")
    public void tokenFilterPostJson(HttpServletRequest request, @RequestBody TokenEntity entity) {
        log.info("json body third time: " + RequestUtil.getRequestParamValue(request, "token"));
//        throw new PhoneAlreadyUsedException();
    }
    
    @GetMapping("/tokenFilter")
    public void tokenFilterGet(HttpServletRequest request, @RequestParam String token) {
//        log.info("json body third time: " + RequestUtil.getRequestParamValue(request, "token"));
        throw new PhoneAlreadyUsedException();
    }
    
    @GetMapping(value = "/transactionalInternalInvoke")
    public void transactionalInternalInvoke() {
        
    }

    @Getter
    @Setter
    private static class TokenEntity {
        private String token;
        private Long id;
    }

    @GetMapping("/singletonCache")
    public Object singletonCache(String key) throws ExecutionException, InterruptedException {
//        return SingletonCache.getInstance().getCache(key);
        return SingletonCache.getCache(key, () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return UUID.randomUUID();
        });
    }

    private static class SingletonCache {
        //expiration
        //concurrent executing(Future)
        //atomic put if absent contains check
        //let spring manage that cache
        private static volatile SingletonCache singletonCache;
        private static final Map<String, Object> cache;
        private SingletonCache() {}

        public static SingletonCache getInstance() {
            if (singletonCache == null) {
                synchronized (SingletonCache.class) {
                    if (singletonCache == null) {
                        singletonCache = new SingletonCache();
                    }
                }
            }

            return singletonCache;
        }

        static {
            cache = new ConcurrentHashMap<>();
        }

        public static Object getCache(String key) {
            Object result = cache.get(key);
            if (result == null) {
                result = UUID.randomUUID();
                cache.putIfAbsent(key, result);
            }
            return result;
        }

        public static <T> Object getCache(String key, Supplier<T> supplier) throws ExecutionException, InterruptedException {
            Object result = cache.get(key);
            if (result == null) {
                FutureTask<UUID> uuidFutureTask = new FutureTask<>(UUID::randomUUID);
                uuidFutureTask.run();
                UUID uuid = uuidFutureTask.get();
                CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(supplier);
                result = completableFuture.get();
                cache.putIfAbsent(key, result);
            }
            
            return result;
        }
    }
    
}
