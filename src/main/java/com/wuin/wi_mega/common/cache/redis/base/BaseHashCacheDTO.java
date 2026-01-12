package com.wuin.wi_mega.common.cache.redis.base;

import lombok.Data;


@Data
public class BaseHashCacheDTO {

    private Long expireMills;

    private String data;

    private String key;

    public boolean hasExpired() {
        return expireMills != null
                && !expireMills.equals(-1L)
                && expireMills.compareTo(System.currentTimeMillis()) < 0;
    }
}
