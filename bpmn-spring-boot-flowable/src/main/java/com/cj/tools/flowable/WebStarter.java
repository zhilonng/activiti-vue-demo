package com.cj.tools.flowable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/10/20 10:20 上午
 */
@SpringBootApplication
@MapperScan("cn.zunyi001.flowable.mapper")
public class WebStarter {

    public static void main(String[] args) {
        SpringApplication.run(WebStarter.class, args);
    }
}
