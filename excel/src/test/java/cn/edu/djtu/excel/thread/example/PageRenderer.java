package cn.edu.djtu.excel.thread.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Compare sequential to Future to CompletionService to CompletableFuture to 
 * optimize page renderer
 * @author zzx
 * @date 2021/3/22
 */
public abstract class PageRenderer {
    interface ImageData {
    }

    interface ImageInfo {
        ImageData downloadImage();
    }

    abstract void renderText(CharSequence s);
    abstract List<ImageInfo> scanForImageInfo(CharSequence s);
    abstract void renderImage(ImageData i);
    
    public void sequentiallyRender(CharSequence source) {
        renderText(source);
        List<ImageInfo> imageInfos = scanForImageInfo(source);
        List<ImageData> imageDataList = new ArrayList<>();
        imageInfos.forEach(i -> imageDataList.add(i.downloadImage()));
        if (imageDataList.size() > 0) {
            imageDataList.forEach(this::renderImage);
        }
    }
    
    public void futureRender(CharSequence source) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 30, 60, 
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
        List<ImageInfo> imageInfos = scanForImageInfo(source);
        List<ImageData> imageDataList = new ArrayList<>();
        List<Future<ImageData>> imageFutures = new ArrayList<>();
        if (imageInfos.size() > 0) {
            imageInfos.forEach(i -> imageFutures.add(executor.submit(i::downloadImage)));
        }
        renderText(source);
        imageFutures.forEach(f -> {
            while (f.isDone()) {
                try {
                    imageDataList.add(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    f.cancel(true);
                    e.printStackTrace();
                }
            }
        });
        
        executor.shutdown();

        if (imageDataList.size() > 0) {
            imageDataList.forEach(this::renderImage);
        }
    } 
    
    public void normalThreadRender(CharSequence source) {
        List<ImageInfo> imageInfos = scanForImageInfo(source);
        if (imageInfos.size() > 0) {
            
            imageInfos.forEach(i -> {
                
//                new Callable<ImageData>(() -> i.downloadImage())
            });
        }
    }
    
    class threadTask extends Thread {
//        public Callable<ImageData> executeTask()
    }
}
