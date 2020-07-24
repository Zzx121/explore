package cn.edu.djtu.excel;

import cn.edu.djtu.excel.common.annotation.Excel;
import cn.edu.djtu.excel.entity.Customer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class AnnotationTest {
    @Test
    void repeatable() {
        Class<Customer> clazz = Customer.class;
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        for (Field field : fields) {
            if (field.isAnnotationPresent(Excel.class)) {
                Excel excel = field.getAnnotation(Excel.class);
                System.out.println(excel);
            }
        }
    }
    
    @Test
    void mathCeilTest() {
//        int result = 200 / 149;
        double result = Math.ceil(201 / 200);
        System.out.println(result);
    }
    
    @Test
    void divideTest() {
        System.out.println(13 / 15 + 1);
    }
}
