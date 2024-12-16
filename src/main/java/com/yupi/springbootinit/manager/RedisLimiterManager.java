//package com.yupi.springbootinit.manager;
//
//
//import com.yupi.springbootinit.common.ErrorCode;
//import com.yupi.springbootinit.exception.ThrowUtils;
//import org.redisson.api.RRateLimiter;
//import org.redisson.api.RateIntervalUnit;
//import org.redisson.api.RateType;
//import org.redisson.api.RedissonClient;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//
///**
// * 限流
// */
//@Service
//public class RedisLimiterManager {
//
//    @Resource
//    private RedissonClient redissonClient;
//
//    /**
//     * 根据用户id进行限流
//     * @param key
//     */
//    public void doRateLimit(String key){
//// 获取限流器，每个用户一个限流器
//        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate_limiter:" + key);
//
//        // 初始化限流器（ 每秒 最多 2 个请求）
//        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
//
//        // 尝试获取一个许可
//        boolean allowed = rateLimiter.tryAcquire(1);
//        ThrowUtils.throwIf(!allowed, ErrorCode.TOO_MANY_REQUEST);
////        if (!allowed) {
////            // 限流了，返回false
////            System.out.println("Rate limit exceeded for user: " + key);
//////            return false;
////
////        }
//
//        // 请求通过，返回true
////        return true;
//    }
//
//}
