package com.xmon.shanlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xmon.shanlink.project.dao.mapper")
public class ShanLinkProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShanLinkProjectApplication.class, args);
    }
}
