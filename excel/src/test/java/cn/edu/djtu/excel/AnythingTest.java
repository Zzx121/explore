package cn.edu.djtu.excel;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnythingTest {
    @Test
    void dateTimeFormat() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        System.out.println(dateTimeFormatter.format(LocalDateTime.now()));
    }
}
