package cn.edu.djtu.db;

/**
 * @author zzx
 * @date 2021/7/23
 */
public class ProxySubject implements Subject {
    private final Subject subject;

    public ProxySubject() {
        this.subject = new RealSubject();
    }

    @Override
    public void request() {
        System.out.println("ProxySubject doing the proxy job");
        subject.request();
    }
}
