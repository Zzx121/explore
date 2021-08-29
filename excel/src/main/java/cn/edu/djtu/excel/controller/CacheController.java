package cn.edu.djtu.excel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zzx
 */
@RestController
@RequestMapping("/cache")
public class CacheController {
    private ConcurrentMap<String, Object> cacheMap = new ConcurrentHashMap<>();
    
    @GetMapping("/getByKey")
    public Object getByKey(String key) {
        Object val = cacheMap.get(key);
        Random random = new Random(100);
        if (val == null) {
            val = random.nextInt();
            cacheMap.put(key, val);
        }
        return val;
    }
}
