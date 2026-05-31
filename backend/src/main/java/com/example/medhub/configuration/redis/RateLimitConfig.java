package com.example.medhub.configuration.redis;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://" + redisHost + ":" + redisPort);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new io.lettuce.core.codec.ByteArrayCodec());
        return LettuceBasedProxyManager.builderFor(connection).build();
    }

    @Bean
    public BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }
}
