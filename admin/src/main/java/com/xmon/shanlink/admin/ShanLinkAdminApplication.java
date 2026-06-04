package com.xmon.shanlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xmon.shanlink.admin.dao.mapper")
public class ShanLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShanLinkAdminApplication.class, args);
    }
}
