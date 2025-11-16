package com.bluemsun.blog.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一将 Long 转为字符串返回，避免前端 JS 精度丢失（Snowflake 等长整型 ID）。
 * 同时注册 JavaTimeModule 以支持 Java 8 时间类型序列化。
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // Long 转字符串，避免前端精度丢失
            SimpleModule longToString = new SimpleModule();
            longToString.addSerializer(Long.class, ToStringSerializer.instance);
            longToString.addSerializer(Long.TYPE, ToStringSerializer.instance);
            
            // 注册 Java 8 时间模块和自定义模块
            builder.modules(new JavaTimeModule(), longToString);
        };
    }
}


