package cn.edu.djtu.excel.common.application;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationUtil.applicationContext = applicationContext;
    }

    /**
     * 获取ApplicationContext
     */
    public static ApplicationContext getContext() {
        return applicationContext;
    }
    
    public static <T> T getBean(Class<T> requiredClass) {
        return getContext().getBean(requiredClass);
    }
}
