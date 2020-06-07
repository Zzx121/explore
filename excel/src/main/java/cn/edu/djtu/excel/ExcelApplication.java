package cn.edu.djtu.excel;

import cn.edu.djtu.excel.config.RibbonConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@RibbonClients(defaultConfiguration = RibbonConfiguration.class)
public class ExcelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelApplication.class, args);
    }

}
