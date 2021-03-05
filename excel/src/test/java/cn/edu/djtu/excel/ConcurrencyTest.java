package cn.edu.djtu.excel;

import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author zzx
 * @date 2020/12/30
 */
public class ConcurrencyTest {
    static final List<Integer> a = Collections.synchronizedList(new ArrayList<>());
    
    @Test
    void runTest() throws InterruptedException {
        int x = 20;
        Thread t = new Thread(() -> addIfAbsent(x));
        t.start();
        addIfAbsent(x);
        t.join();
        System.out.println(a);
    }
    
    private void addIfAbsent(int x) {
        synchronized (a) {
            if (!a.contains(x)) {
                a.add(x);
            }
        }
    }
    
    @Test
    void setTest() {
        Set<String> s1 = new HashSet<>();
        Set<String> s2 = new HashSet<>();
        List<String> l1 = Arrays.asList("z", "x", "y", "t", "a", "b");
        s1.add("a");
        s1.add("b");
        s1.add("c");
        s2.add("e");
        s2.add("c");
        s2.add("a");
        s2.addAll(s1);
        s1.addAll(l1);
//        boolean b = s1.retainAll(s2);
        
        System.out.println(s1);
        System.out.println(s2);
    }
}
