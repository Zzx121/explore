package cn.edu.djtu.excel.util.basic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author zzx
 * @date 2021/8/13
 */
public class RequestUtil {
    public static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    /**
     * 从request中获取参数值
     * 获取顺序为 1.header 2.query-string 或 form-data 3.body payload
     *
     * @param paramKey 参数key
     * @param request  servlet request
     * @return 参数值
     */
    public static String getRequestParamValue(ServletRequest request, String paramKey) {
        if (request == null || paramKey == null) {
            return null;
        }

        // header
        String headerValue = ((HttpServletRequest) request).getHeader(paramKey);
        if (StringUtils.isNotEmpty(headerValue)) {
            return headerValue;
        }

        // query-string 或 form-data
        String[] paramArr = request.getParameterValues(paramKey);
        if (paramArr != null && paramArr.length > 0) {
            String paramValue = paramArr[0];
            if (StringUtil.isNotEmpty(paramValue))
                return paramValue;
        }

        // body payload(json)
        try {
            Map<String, String> bodyMap = new Gson().fromJson(request.getReader(), new TypeToken<Map<String, String>>() {
            }.getType());
            if (bodyMap != null && bodyMap.size() > 0) {
                String paramValue = bodyMap.get(paramKey);
                if (StringUtil.isNotEmpty(paramValue))
                    return paramValue;
            }
        } catch (IOException e) {
            logger.error("解析body payload失败", e);
        }
        return null;
    }
}
