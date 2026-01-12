package com.wuin.wi_mega.common.cache.redis;

import com.wuin.wi_mega.common.cache.redis.base.ObjectCache;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserAuthCache extends ObjectCache<AppUserDO> {

    @Override
    protected String prefix() {
        return "MG:AUTH";
    }

    @Override
    protected Long timeout() {
        return 60 * 60 * 1000L; //1小时有效期
    }

    /**
     * 根据用户ID删除该用户所有的登录缓存
     * 注意：此方法会遍历所有token缓存，在token数量较多时可能有性能影响
     * @param userId 用户ID
     */
    public void removeByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String pattern = prefix() + ":*";
            RKeys keys = redissonClient.getKeys();
            Iterable<String> keysByPattern = keys.getKeysByPattern(pattern);
            int removedCount = 0;
            for (String key : keysByPattern) {
                try {
                    // 从完整key中提取token
                    String token = key.replace(prefix() + ":", "");
                    AppUserDO user = this.get(token);
                    if (user != null && userId.equals(user.getId())) {
                        this.remove(token);
                        removedCount++;
                        log.info("removeByUserId -> 已删除用户登录缓存, userId={}, token={}", userId, token);
                    }
                } catch (Exception e) {
                    log.warn("removeByUserId -> 处理key异常, key={}, error={}", key, e.getMessage());
                }
            }
            log.info("removeByUserId -> 完成, userId={}, removedCount={}", userId, removedCount);
        } catch (Exception e) {
            log.error("removeByUserId -> 删除用户缓存异常, userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}
