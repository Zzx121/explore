package cn.edu.djtu.excel.common.annotation;


import java.lang.annotation.*;

/**
 * Excel 自定义注解
 * @author zzx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Excels.class)
public @interface Excel {
    /**
     * 表头对应名称
     */
    String name() default "";
    String dateFormat() default "";

    /**
     * 读取内容转换表达式(如：0=男,1=女,2=未知)，用于处理固定不变的对应关系
     */
    String readConverterExp() default "";
    
    /**
     * 读取内容转换，用于处理需要实时传递的对应关系
     */
    String readConverterKey() default "";

    /**
     * 列高度 单位为字符
     */
    double height() default 14;
    /**
     * 列宽度 单位为字符
     */
    double width() default 16;

    /**
     * 文字后缀，如%,元
     */
    String suffix() default "";

    /**
     * 当值为空时，字段的默认值
     */
    String defaultValue() default "";

    /**
     * 提示信息
     */
    String prompt() default "";

    /**
     * 设置只能选择不能输入的列内容.
     */
    String[] combo() default {};

    /**
     * 是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.
     */
    boolean isExport() default true;

    /**
     * 另一个类中的属性名称,支持多级获取,以小数点隔开
     */
    String targetAttr() default "";

    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    Type type() default Type.ALL;

    /**
     * 表头前景颜色，默认黄色
     */
    short headerForegroundColor() default 13;

    enum Type {
        ALL(0), EXPORT(1), IMPORT(2);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        int value() {
            return this.value;
        }
    }
}
