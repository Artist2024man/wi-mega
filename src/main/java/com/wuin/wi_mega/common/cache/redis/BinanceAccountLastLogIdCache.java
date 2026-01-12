package com.wuin.wi_mega.common.cache.redis;

import com.wuin.wi_mega.common.cache.redis.base.HashCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class BinanceAccountLastLogIdCache extends HashCache<Long> {

    @Override
    protected String prefix() {
        return "binance:log_id";
    }

    @Override
    protected Long timeout() {
        return null;
    }

}
