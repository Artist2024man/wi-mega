package com.wuin.wi_mega.common.cache.redis.base;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

public abstract class ObjectCache<V> extends BaseCache<V> {

    @Autowired
    protected RedissonClient redissonClient;

    protected String prefix() {
        return this.getClass().getSimpleName().toUpperCase();
    }

    /**
     * 过期时间，单位：秒
     */
    protected abstract Long timeout();

    public void set(String key, V value) {
        RBucket<String> bucket = redissonClient.getBucket(getKey(key));
        if (null != timeout()) {
            bucket.set(super.toJSON(value), Duration.ofSeconds(timeout()));
        } else {
            bucket.set(super.toJSON(value));
        }
    }

    public void remove(String... keys) {
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            RBucket<String> bucket = redissonClient.getBucket(getKey(key));
            bucket.delete();
        }
    }

    public V get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(getKey(key));
        String value = bucket.get();
        if (null != timeout() && null != value) {
            bucket.expire(Duration.ofSeconds(timeout()));
        }
        return StringUtils.isBlank(value) ? null : fromJSON(value, supportClazz);
    }

    protected String getKey(String key) {
        return prefix().concat(":").concat(null == key ? "null" : key);
    }

}
