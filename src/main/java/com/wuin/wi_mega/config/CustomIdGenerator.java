package com.wuin.wi_mega.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import org.springframework.stereotype.Component;

@Component
public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return SimpleSnowflake.nextId();
    }

    @Override
    public String nextUUID(Object entity) {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
