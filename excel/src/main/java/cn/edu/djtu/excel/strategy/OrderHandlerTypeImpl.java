package cn.edu.djtu.excel.strategy;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author zzx
 * @date 2020/11/2
 */
public class OrderHandlerTypeImpl implements OrderHandlerType {
    
    private String source;
    /**
     * 支付方式
     */
    private String payMethod;
    @Override
    public String source() {
        return source;
    }

    @Override
    public String payMethod() {
        return payMethod;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OrderHandlerType.class;
    }

    public OrderHandlerTypeImpl(String source, String payMethod) {
        this.source = source;
        this.payMethod = payMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderHandlerType)) return false;
        OrderHandlerType that = (OrderHandlerType) o;
        return source.equals(that.source()) && payMethod.equals(that.payMethod());
    }

    /**
     * The hash code of an annotation is the sum of the hash codes of its members (including those with default values),
     * as defined below: The hash code of an annotation member is (127 times the hash code of the member-name as computed
     * by String.hashCode()) XOR the hash code of the member-value, as defined below:
     * @return hash
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += (127 * "source".hashCode()) ^ source.hashCode();
        hashCode += (127 * "payMethod".hashCode()) ^ payMethod.hashCode();
        return hashCode;
    }
}
