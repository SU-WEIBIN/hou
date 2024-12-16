//package com.yupi.springbootinit.config;
//
//
//import lombok.Data;
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConfigurationProperties(prefix = "redis")
//@Data
//public class RedissonConfig {
//
//    private Integer port;
//    private String host;
//    private Integer database;
////    private String password;
//    public RedissonClient redissonClient(){
//        Config config = new Config();
//        config.useSingleServer()
//                .setDatabase(1)
//                .setAddress("redis://"+host+":"+port);
////                .setPassword(password);
//        return Redisson.create(config);
//    }
//}
