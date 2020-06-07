package cn.edu.djtu.db;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan("cn.edu.djtu.db.dao")
public class DbApplication implements CommandLineRunner {
    @Autowired
    DataSource dataSource;
    public static void main(String[] args) {
        SpringApplication.run(DbApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataSource ==== "+ dataSource);
    }
}
