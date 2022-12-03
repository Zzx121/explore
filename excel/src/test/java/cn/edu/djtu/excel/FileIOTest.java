package cn.edu.djtu.excel;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName FileIOTest
 * @Description: TODO
 * @Author zzx
 * @Date 2022/10/5
 **/
public class FileIOTest {
    @Test
    void readFileStreamWay() {
        try(BufferedReader br = new BufferedReader(new FileReader("csvDemo.csv"))) {
            Stream<String> lines = br.lines();
            System.out.println(lines.count());
            System.out.println(lines.skip(3).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void readFile() {
        List<String> lines = new ArrayList<>();
        List<Poi> poiList = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader("csvDemo.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
//            System.out.println(lines);
            Poi poi;
            for (String l : lines) {
                String[] split = l.split(",");
                poi = new Poi();
                poi.setId(Long.valueOf(split[0]));
                poi.setName(split[1]);
                poiList.add(poi);
            }
            
            //Concurrency invoking to store in database like storage
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            int pace = 3;
            int size = poiList.size();
            for (int i = 0; i < size; i += pace) {
                List<Poi> subList = poiList.subList(i, Math.min((i + pace), size));
                executorService.execute(() -> insert(subList));
//                executorService.submit(() -> insert(subList));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    void insert(List<Poi> poiList) {
        System.out.println(poiList);
    }
    @Data
    public static class Poi {
        private Long id;
        private String name;
        private String address;
        private String email;
        
        @Override
        public String toString() {
            return id + "," + name + "," + address + "," + email + "\n"; 
        }
    }
    
    void concurrentCalling() {
//        Executors.newFixedThreadPool()
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 15, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
        Future<?> submit = tpe.submit(this::readFile);
        try {
            Object o = submit.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        tpe.execute(this::readFile);
    }
    
    @Test
    void fileChannelTest() {
//        try(SeekableByteChannel seekableByteChannel = Files.newByteChannel(Path.of("csvDemo.csv"))) {
//            System.out.println(seekableByteChannel.size());
//        } catch (IOException ex) {
//          ex.printStackTrace();  
//        }
        
        try (FileChannel fileChannel = new RandomAccessFile(new File("5million.csv"), "r").getChannel()) {
//            FileChannel positionedChannel = fileChannel.position(228);
            long size = fileChannel.size();
            int tailBytesLength = 0;
            long dividedSize = size / 10;

            ByteBuffer allocate = ByteBuffer.allocate((int) dividedSize);
            int tailBytesLengthSum = 0;
            StringBuffer decodedString = new StringBuffer();
            for (int i = 0; i < 10; i++) {
                FileChannel positionedFC = fileChannel;
                if (i > 0 && tailBytesLength > 0) {
                    positionedFC = fileChannel.position(dividedSize * i - tailBytesLengthSum);
                }
//            CharBuffer charBuffer = CharBuffer.allocate(1024);
                if (positionedFC.read(allocate) > 0) {
                    allocate.rewind();
                    // There just another way to achieve this, just convert the '\n' to bytes and then compare TODO
                    CharBuffer decodedCharBuffer = StandardCharsets.UTF_8.decode(allocate);
                    decodedString.append(decodedCharBuffer.array());
//                    String s = decodedCharBuffer.toString();
                    int breakRowIndex = decodedString.lastIndexOf("\n");
                    //String need to change to StringBuilder to save the memory, don't forget to free after usage
                    String tailString = decodedString.substring(breakRowIndex);
                    if (!"".equals(tailString)) {
                        ByteBuffer tailBuffer = StandardCharsets.UTF_8.encode(tailString);
                        tailBytesLength = tailBuffer.array().length;
                        tailBytesLengthSum += tailBytesLength;
                    }
                    allocate.flip();
                    // Dealing the retrieved string and separate into rows to put into Poi list
                    decodedString.delete(breakRowIndex, decodedString.length());
                    // Whether this way is more efficient need testing.
                    String[] rowsArray = Pattern.compile("\n").split(decodedString);
                }
            }
            
            
            // Separate and cache the rows in file
            // The dealing of find index of the '\n' '\r', the difference between char and byte
            // Maximum size of one row, then multiply the required rows count 
            // Find the near one row and record the row number and then return these rows back
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        
    }
    
    @Test
    void differenceBetweenByteAndChar() {
        Byte b = Byte.valueOf("23");
        byte[] bytes = {2, 3, 5};
        char c1 = '\u3212';
        char c3 = '\u3012';
        String s1 = String.valueOf(new char[]{c1, c3});
        System.out.println(s1);
        char c2 = 32424;
        Character character = '\u10DC';
        System.out.println(b);
        System.out.println(Arrays.toString(bytes));
        System.out.println(c1);
        System.out.println(c2);
        System.out.println(character);
        //Convert bytes to String or chars
        byte[] randomBytes = {52, 2, -32, 5, 9, -34, 20, -87, 32};
        System.out.println(Arrays.toString(s1.getBytes()));
        System.out.println(Charset.defaultCharset().decode(ByteBuffer.wrap(randomBytes)));
    }
    
    @Test
    void generateData() throws IOException {
//        List<Poi> poiList = new ArrayList<>();
        Poi poi;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("5million.csv"));
        for (int i = 0; i < 5000000; i++) {
            poi = new Poi();
            poi.setId((long) i);
            poi.setAddress("高教大楼" + i);
            poi.setEmail(i + "@qq.com");
            poi.setName("Sam Smith " + i);
            bufferedWriter.append(poi.toString());
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }
    
    @Test
    void scannerTest() {
        System.setIn(new ByteArrayInputStream("Today is warmer than last days.\r\n".getBytes()));
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
//        Pattern.compile("\\s+")
        int lastSpaceIndex = line.lastIndexOf(" ");
        int length = line.length();
        int lastWordLength = length - lastSpaceIndex - 1;
        System.out.println(lastWordLength);
        CompletableFuture<String> future = new CompletableFuture<>();
    }
}
