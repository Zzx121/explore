package cn.edu.djtu.excel.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @date 2020/11/2
 */
@Service
public class OrderService {
    private Map<OrderHandlerType, OrderHandler> orderHandlerMap;

    /**
     * 注解类加入了@service，所以会自动被spring 容器管理
     */
    @Autowired
    public void setOrderHandlerMap(List<OrderHandler> orderHandlers) {
        this.orderHandlerMap = orderHandlers.stream().collect(Collectors.toMap(orderHandler -> 
                        Objects.requireNonNull(AnnotationUtils.findAnnotation(orderHandler.getClass(),
                                OrderHandlerType.class)), 
                v -> v, (v1, v2) -> v1));
    }
    
    public void orderService(Order order) {
        OrderHandler orderHandler = orderHandlerMap.get(new OrderHandlerTypeImpl(order.getSource(), order.getPayMethod()));
        orderHandler.handle(order);
    }
}
