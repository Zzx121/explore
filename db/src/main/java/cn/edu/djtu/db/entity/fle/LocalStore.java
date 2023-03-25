package cn.edu.djtu.db.entity.fle;

/**
 * @ClassName LocalStore
 * @Description: TODO
 * @Author zzx
 * @Date 2023/2/8
 **/
public class LocalStore {
    private ThreadLocal<Borders> bordersThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Payload> localPayload = new ThreadLocal<>();

}
