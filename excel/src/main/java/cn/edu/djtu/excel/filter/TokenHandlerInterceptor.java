package cn.edu.djtu.excel.filter;

import cn.edu.djtu.excel.util.basic.RequestUtil;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zzx
 * @date 2021/8/12
 */
@Log
public class TokenHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        log.info("parameter first time: " + request.getParameter("token"));
        log.info("json body second time: " + RequestUtil.getRequestParamValue(request, "token"));
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
    
}
