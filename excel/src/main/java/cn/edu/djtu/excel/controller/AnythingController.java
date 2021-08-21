package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.common.exception.PhoneAlreadyUsedException;
import cn.edu.djtu.excel.util.basic.RequestUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
    
}
