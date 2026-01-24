package com.xunjiqianxing.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 小程序后端启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.xunjiqianxing")
@MapperScan("com.xunjiqianxing.**.mapper")
@EnableScheduling
public class XjAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(XjAppApplication.class, args);
        System.out.println("====================================");
        System.out.println("  寻迹千行 小程序后端启动成功!");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("====================================");
    }
}
