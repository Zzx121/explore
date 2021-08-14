package cn.edu.djtu.excel.filter;

import cn.edu.djtu.excel.util.basic.CacheRequestWrapper;
import cn.edu.djtu.excel.util.basic.RequestUtil;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zzx
 * @date 2021/8/12
 */
@Log
@Component
public class TokenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(request);
        log.info("parameter first time: " + request.getParameter("token"));
        CacheRequestWrapper requestWrapper = new CacheRequestWrapper((HttpServletRequest) request);
        log.info("json body first time: " + RequestUtil.getRequestParamValue(requestWrapper, "token"));
        chain.doFilter(requestWrapper, response);
    }
}
