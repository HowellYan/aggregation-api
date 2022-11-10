package com.atomscat.bootstrap.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Howell.Yang
 */
@Configuration
@MapperScan("com.atomscat.bootstrap.modules.*.*.mapper")
public class MybatisPlusConfig {

}
