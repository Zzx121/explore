package cn.edu.djtu.excel.strategy;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author zzx
 * @date 2020/11/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface OrderHandlerType {
    String source();
    String payMethod();
}
