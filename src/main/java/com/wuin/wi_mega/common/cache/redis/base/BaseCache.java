package com.wuin.wi_mega.common.cache.redis.base;

import com.alibaba.fastjson2.JSON;
import org.springframework.boot.CommandLineRunner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class BaseCache<T> implements CommandLineRunner {

    protected Class<T> supportClazz;

    /**
     * Object转成JSON数据
     */
    protected String toJSON(Object object) {
        return isBaseClazz(object.getClass()) ? String.valueOf(object) : JSON.toJSONString(object);
    }

    /**
     * JSON数据，转成Object
     */
    protected T fromJSON(String json, Class<T> clazz) {
        return clazz.equals(String.class) ? (T)json : JSON.parseObject(json, clazz);
    }

    private Boolean isBaseClazz(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Long.class ||
                clazz == Integer.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == String.class ||
                clazz == Map.class ||
                clazz == Collection.class ||
                clazz == BigDecimal.class;
    }

    @Override
    public void run(String... args) throws Exception {
        Type genType = this.getClass().getGenericSuperclass();
        Class<T> desClazz = (Class<T>) ((ParameterizedType) genType).getActualTypeArguments()[0];
        supportClazz = desClazz;
    }
}
