package cn.edu.djtu.excel.strategy;

/**
 * @author zzx
 * @date 2020/11/2
 */
@OrderHandlerType(source = "pc", payMethod = "alipay")
public class PcOrderHandler implements OrderHandler {
    @Override
    public void handle(Order order) {
        System.out.println("处理PC端支付宝订单");
    }
}
