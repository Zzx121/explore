package cn.edu.djtu.db;

import lombok.extern.java.Log;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author zzx
 * @date 2021/7/23
 */
@Log
public class RealSubject implements Subject {
//    private Subject subject = new RealSubject();

    @Override
    public void request() {
        System.out.println("RealSubject request invoked");
    }

    public static void main(String[] args) {
        Subject subject = (Subject) Proxy.newProxyInstance(Subject.class.getClassLoader(),
                new Class<?>[]{Subject.class, Map.class}, (proxy, method, args1) -> {
                    System.out.println("InvocationHandler processed");
                    if (method.getName().equals("get")) {
                        log.info("12");
                        return 12;
                    } else if (method.getName().equals("request")) {
                        method.invoke(new ProxySubject(), args1);
                        return 10;
                    }
                    return 5;
                });
 
//        System.out.println(subject.put("a", 1));
//        System.out.println(subject.get("a"));
        subject.request();
    }
}
