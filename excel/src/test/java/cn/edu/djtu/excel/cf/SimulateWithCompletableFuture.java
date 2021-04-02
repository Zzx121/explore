package cn.edu.djtu.excel.cf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author zzx
 * @date 2021/3/10
 */
public class SimulateWithCompletableFuture {
    private static void simulateDelay() throws InterruptedException {
        Thread.sleep(5000);
    }

    static List<Car> cars() {
        List<Car> carList = new ArrayList<>();
        carList.add(new Car(1, 3, "Fiesta", 2017));
        carList.add(new Car(2, 7, "Camry", 2014));
        carList.add(new Car(3, 2, "M2", 2008));
        return carList;
    }

    static float rating(int manufacturer) {
        try {
            simulateDelay();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        switch (manufacturer) {
            case 2:
                return 4f;
            case 3:
                return 4.1f;
            case 7:
                return 4.2f;
            default:
                return 5f;
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<Car> cars = cars();
        CompletableFuture<Car> carCompletableFuture = new CompletableFuture<>();
        cars.forEach(car -> carCompletableFuture.thenAcceptAsync(c -> c.setRating(rating(c.manufacturerId))));
        carCompletableFuture.join();
        cars.forEach(System.out::println);

        long end = System.currentTimeMillis();
        System.out.println("Took " + (end - start) + " ms.");
//        carCompletableFuture
    }
}
