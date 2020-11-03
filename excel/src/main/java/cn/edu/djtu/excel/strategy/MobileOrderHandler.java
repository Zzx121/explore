package cn.edu.djtu.excel.strategy;

/**
 * @author zzx
 * @date 2020/11/2
 */
@OrderHandlerType(source = "mobile", payMethod = "wechat")
public class MobileOrderHandler implements OrderHandler {
    @Override
    public void handle(Order order) {
        System.out.println("处理移动端微信订单");
    }
}
