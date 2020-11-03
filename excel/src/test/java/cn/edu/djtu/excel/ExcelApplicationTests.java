package cn.edu.djtu.excel;

import cn.edu.djtu.excel.strategy.Order;
import cn.edu.djtu.excel.strategy.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExcelApplicationTests {
    @Autowired
    OrderService orderService;
    @Test
    void strategyTest() {
        orderService.orderService(Order.builder().source("pc").payMethod("alipay").build());
    }
}
