package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.common.exception.PhoneAlreadyUsedException;
import cn.edu.djtu.excel.util.basic.RequestUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    public Object singletonCache(String key) {
//        return SingletonCache.getInstance().getCache(key);
        return SingletonCache.getInstance().getCache(key, UUID::randomUUID);
    }

    private static class SingletonCache {
        private static volatile SingletonCache singletonCache;
        private ConcurrentMap<String, Object> cache;
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

        {
            cache = new ConcurrentHashMap<>();
        }

        public Object getCache(String key) {
            Object result = cache.get(key);
            if (result == null) {
                result = UUID.randomUUID();
                cache.put(key, result);
            }
            return result;
        }

        public Object getCache(String key, Supplier<Object> supplier) {
            Object result = cache.get(key);
            if (result == null) {
                result = supplier.get();
                cache.put(key, result);
            }
            return result;
        }


    }
    
}
