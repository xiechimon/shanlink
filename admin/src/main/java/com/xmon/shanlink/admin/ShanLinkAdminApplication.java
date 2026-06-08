package com.xmon.shanlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.xmon.shanlink.admin.dao.mapper")
@EnableFeignClients(basePackages = "com.xmon.shanlink.admin.remote")
public class ShanLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShanLinkAdminApplication.class, args);
    }
}
