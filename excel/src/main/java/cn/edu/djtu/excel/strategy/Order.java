package cn.edu.djtu.excel.strategy;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @date 2020/11/2
 */
@Data
@Builder
public class Order {
    /**
     * 订单来源
     */
    private String source;
    /**
     * 支付方式
     */
    private String payMethod;
    /**
     * 订单编号
     */
    private String code;
    /**
     * 订单金额
     */
    private BigDecimal amount;
}
