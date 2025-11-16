package com.bluemsun.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class BluemsunBlogApplication {

    /**
     * 主入口方法，直接运行即可启动后端服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BluemsunBlogApplication.class, args);
    }
}

