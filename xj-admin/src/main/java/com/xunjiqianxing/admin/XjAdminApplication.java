package com.xunjiqianxing.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 管理后台启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.xunjiqianxing")
@MapperScan("com.xunjiqianxing.**.mapper")
@EnableScheduling
public class XjAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(XjAdminApplication.class, args);
        System.out.println("====================================");
        System.out.println("  寻迹千行 管理后台启动成功!");
        System.out.println("  API文档: http://localhost:8081/doc.html");
        System.out.println("====================================");
    }
}
