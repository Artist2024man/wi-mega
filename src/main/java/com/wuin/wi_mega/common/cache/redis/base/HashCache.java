package com.wuin.wi_mega.common.cache.redis.base;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class HashCache<V> extends BaseCache<V> {

    @Autowired
    private RedissonClient redissonClient;

    protected abstract String prefix();

    protected abstract Long timeout();

    public V get(String key) {
        RMap<String, String> map = redissonClient.getMap(prefix());
        String result = map.get(key);
        if (StringUtils.isNotBlank(result)) {
            BaseHashCacheDTO cacheDTO = JSON.parseObject(result, BaseHashCacheDTO.class);
//            log.info("get auth info -> key = {}, data={}, cacheDTO={}", key, result, JSON.toJSONString(cacheDTO));
            if (cacheDTO.hasExpired() || StringUtils.isBlank(cacheDTO.getData())) {
                this.remove(key);
                return null;
            }
            return fromJSON(cacheDTO.getData(), supportClazz);
        }
        return null;
    }

    public List<V> multiGet(List<String> keys) {
        RMap<String, String> map = redissonClient.getMap(prefix());
        Map<String, String> resMap = map.getAll(new HashSet<>(keys));
        if (MapUtils.isEmpty(resMap)) {
            return new ArrayList<>();
        } else {
            List<BaseHashCacheDTO> cacheList = resMap.values().stream()
                    .filter(Objects::nonNull)
                    .map(val -> JSON.parseObject(val, BaseHashCacheDTO.class)).collect(Collectors.toList());

            List<BaseHashCacheDTO> expireList = cacheList.stream()
                    .filter(dto -> dto.hasExpired() || StringUtils.isBlank(dto.getData()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(expireList)) {
                this.remove(expireList.stream().map(BaseHashCacheDTO::getKey).toArray(String[]::new));
                cacheList.removeAll(expireList);
            }
            return CollectionUtils.isEmpty(cacheList) ? new ArrayList<>()
                    : cacheList.stream().map(dto -> super.fromJSON(dto.getData(), supportClazz)).collect(Collectors.toList());
        }
    }

    public void put(String key, V value) {
        if (null == value) {
            throw new IllegalArgumentException("value can not be null");
        }
        BaseHashCacheDTO hashCacheDTO = this.buildBaseHashCacheDTO(key, value);
        RMap<String, String> map = redissonClient.getMap(prefix());
        map.put(key, JSON.toJSONString(hashCacheDTO));
    }

    public void putAll(Map<String, V> valueMap) {
        Map<String, String> strValueMap = new HashMap<>();
        valueMap.forEach((key, vale) -> {
            BaseHashCacheDTO cacheDTO = this.buildBaseHashCacheDTO(key, vale);
            strValueMap.put(key, super.toJSON(cacheDTO));
        });
        RMap<String, String> map = redissonClient.getMap(prefix());
        map.putAll(strValueMap);
    }

    public void remove(String... keys) {
        if (null == keys || keys.length < 1) {
            return;
        }
        RMap<String, String> map = redissonClient.getMap(prefix());
        map.fastRemove(keys);
    }

    public List<String> keys() {
        RMap<String, String> map = redissonClient.getMap(prefix());
        return new ArrayList<>(map.keySet());
    }

    public List<V> values() {
        RMap<String, String> map = redissonClient.getMap(prefix());
        List<String> valueList = new ArrayList<>(map.values());
        if (CollectionUtils.isEmpty(valueList)) {
            return new ArrayList<>();
        } else {
            List<BaseHashCacheDTO> resList = valueList.stream().map(value -> JSON.parseObject(value, BaseHashCacheDTO.class))
                    .collect(Collectors.toList());
            return resList.stream().map(dto -> fromJSON(dto.getData(), supportClazz)).collect(Collectors.toList());
        }
    }

    public void clearExpireKey() {
        RMap<String, String> map = redissonClient.getMap(prefix());
        List<String> needRemoveKeys = new ArrayList<>();
        map.forEach((key, value) -> {
            BaseHashCacheDTO cacheDTO = JSON.parseObject(value, BaseHashCacheDTO.class);
            if (cacheDTO.hasExpired()) {
                needRemoveKeys.add(key);
            }
        });
        map.fastRemove(needRemoveKeys.toArray(new String[0]));
    }

    protected BaseHashCacheDTO buildBaseHashCacheDTO(String key, V value) {
        BaseHashCacheDTO cacheDTO = new BaseHashCacheDTO();
        cacheDTO.setData(toJSON(value));
        cacheDTO.setKey(key);
        if (null != timeout() && !timeout().equals(-1L)) {
            cacheDTO.setExpireMills(System.currentTimeMillis() + timeout());
        }
        return cacheDTO;
    }


}
