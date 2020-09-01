package cn.edu.djtu.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EasyExcelTest {
    @Test
    void simpleWrite() {
        String fileName = getPath() + "simpleWrite" + System.currentTimeMillis() + '.' + ExcelTypeEnum.XLSX.getValue();
        EasyExcel.write(fileName, DemoData.class).sheet().doWrite(data(20));
    }
    
    private String getPath() {
        return this.getClass().getResource("/").getPath();
    }
    
    private List<DemoData> data(int rows) {
        List<DemoData> result = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            DemoData demoData = new DemoData();
            demoData.setDate(new Date());
            demoData.setDoubleData(Math.random() * rows);
            demoData.setIgnore("Ignored" + i);
            demoData.setTitle("Title" + i);
            demoData.setLocalDate(LocalDate.now());
            demoData.setLocalDateTime(LocalDateTime.now());
            result.add(demoData);
        }
        
        return result;
    }
    
    @Test
    void LocalDateTimeFormatTest() {
    System.out.println(LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_ZONED_DATE_TIME));
        ImmutableMap<Integer, String> of = ImmutableMap.of(1, "one", 2, "tow", 3, "three");
        Map<Integer, String> ten = Map.of(10, "ten");
    }
    
    @Test
    void StringContainTest() {
        String s = "SAM SMITH";
        System.out.println(s.contains("IT"));
    }
    
    @Test
    void fileDownloadTest() throws IOException {
        URL url = new URL("http://rec1.1ketong.com:8090/pull1/256/20200801/202008010756271212625022220.mp3");
        BufferedInputStream bi = new BufferedInputStream(url.openStream());
        System.out.println(bi.available());
        FileOutputStream fos = new FileOutputStream("tmp.mp3");
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
    
    @Test
    void substringTest() {
        String url = "http://rec1.1ketong.com:8090/pull1/256/20200801/202008010756271212625022220.mp3";
        System.out.println(url.substring(url.lastIndexOf(".")));
    }
}
