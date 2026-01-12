package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.common.enums.SyncStatusEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.SessionAmtBO;
import com.wuin.wi_mega.model.bo.StrategyStatBO;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.mapper.AppAccountSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class AppAccountSessionRepositoryImpl extends ServiceImpl<AppAccountSessionMapper, AppAccountSessionDO> implements AppAccountSessionRepository {

    @Autowired
    private CacheManager cacheManager;

    private static final String SESSION_CACHE = "sessionCache";
    private static final String ACCOUNT_SESSION_LIST_CACHE = "accountSessionListCache";

    @Override
    public List<AppAccountSessionDO> listByAccountIdsAndStatusList(Collection<Long> accountIds, List<Integer> statusList) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AppAccountSessionDO::getAccountId, accountIds);
        wrapper.in(AppAccountSessionDO::getStatus, statusList);
        wrapper.ge(AppAccountSessionDO::getUpdateTime, LocalDateTime.now().minusHours(24));
        return list(wrapper);
    }

    @Override
    public List<AppAccountSessionDO> listByNextCheckTimeAndStatusList(LocalDateTime currentTime, List<Integer> statusList) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(AppAccountSessionDO::getNextCheckTime, currentTime);
        wrapper.in(AppAccountSessionDO::getStatus, statusList);
        return list(wrapper);
    }

    @Override
    public List<AppAccountSessionDO> listByAccountIdAndStatusList(Long accountId, List<Integer> statusList) {
        // 先尝试从缓存获取
        Cache cache = cacheManager.getCache(ACCOUNT_SESSION_LIST_CACHE);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(accountId);
            if (valueWrapper != null) {
                @SuppressWarnings("unchecked")
                List<AppAccountSessionDO> cachedResult = (List<AppAccountSessionDO>) valueWrapper.get();
                log.info("listByAccountIdAndStatusList -> 命中缓存, accountId={}, size={}", accountId, cachedResult != null ? cachedResult.size() : 0);
                return cachedResult;
            }
        }

        // 缓存未命中，查询数据库
        log.info("listByAccountIdAndStatusList -> 缓存未命中，查询数据库, accountId={}", accountId);
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAccountSessionDO::getAccountId, accountId);
        wrapper.in(AppAccountSessionDO::getStatus, statusList);
        wrapper.orderByDesc(AppAccountSessionDO::getId);
        List<AppAccountSessionDO> result = list(wrapper);

        // 存入缓存
        if (cache != null && result != null && !result.isEmpty()) {
            cache.put(accountId, result);
            log.info("listByAccountIdAndStatusList -> 结果存入缓存, accountId={}, size={}", accountId, result.size());
        }

        return result;
    }

    public List<AppAccountSessionDO> listByAccountIdAndStatusListNoCache(Long accountId, List<Integer> statusList) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAccountSessionDO::getAccountId, accountId);
        wrapper.in(AppAccountSessionDO::getStatus, statusList);
        wrapper.orderByDesc(AppAccountSessionDO::getId);
        wrapper.last("limit 30");
        return list(wrapper);
    }

    @Override
    public List<Long> listIdBySymbol(SymbolEnum symbol, List<Integer> statusList) {
        return baseMapper.listIdBySymbol(symbol.name(), statusList);
    }

    @Override
    public List<AppAccountSessionDO> listNeedSyncByStatusList(List<Integer> statusList, LocalDateTime minUpdateTime) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AppAccountSessionDO::getStatus, statusList);
        wrapper.eq(AppAccountSessionDO::getSyncStatus, SyncStatusEnum.NO_SYNC.code());
        wrapper.ge(AppAccountSessionDO::getUpdateTime, minUpdateTime);
        wrapper.le(AppAccountSessionDO::getNextCheckTime, LocalDateTime.now());
        return list(wrapper);
    }

    @Override
    public AppAccountSessionDO getLastByAccountId(Long accountId) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAccountSessionDO::getAccountId, accountId);
        wrapper.orderByDesc(AppAccountSessionDO::getId);
        wrapper.last("limit 1");
        return getOne(wrapper);
    }

    @Override
    public SessionAmtBO sumAmtByAccount(Long accountId, LocalDateTime minCreateTime, Long minId, List<Integer> statusList) {
        return baseMapper.sumAmtByAccount(accountId, minCreateTime, minId, statusList);
    }

    @Override
    public IPage<AppAccountSessionDO> pageList(Long accountId, Integer status, LocalDateTime startTime,
                                               LocalDateTime endTime, String symbol, int page, int pageSize) {
        LambdaQueryWrapper<AppAccountSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAccountSessionDO::getAccountId, accountId);
        if (status != null) {
            wrapper.eq(AppAccountSessionDO::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(AppAccountSessionDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AppAccountSessionDO::getCreateTime, endTime);
        }
        if (symbol != null && !symbol.isEmpty()) {
            wrapper.eq(AppAccountSessionDO::getSymbol, symbol);
        }
        wrapper.orderByDesc(AppAccountSessionDO::getId);
        Page<AppAccountSessionDO> pageParam = new Page<>(page, pageSize);
        return page(pageParam, wrapper);
    }

    @Override
    public StrategyStatBO statByAccount(Long accountId, LocalDateTime minCreateTime, List<Integer> statusList) {
        StrategyStatBO result = baseMapper.statByAccount(accountId, minCreateTime, statusList);
        if (result == null) {
            result = new StrategyStatBO();
        }
        return result;
    }

    @Override
    public boolean save(AppAccountSessionDO entity) {
        boolean result = super.save(entity);
        if (result && entity.getAccountId() != null) {
            evictAllCaches(entity.getId(), entity.getAccountId());
            log.warn("save -> 保存会话并清除缓存, sessionId={}, accountId={}", entity.getId(), entity.getAccountId());
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        // 先获取session信息以获取accountId
        AppAccountSessionDO existingSession = baseMapper.selectById(id);
        boolean result = super.removeById(id);
        if (result && existingSession != null && existingSession.getAccountId() != null) {
            evictAllCaches((Long) id, existingSession.getAccountId());
            log.warn("removeById -> 删除会话并清除缓存, sessionId={}, accountId={}", id, existingSession.getAccountId());
        }
        return result;
    }

    @Override
    public boolean updateById(AppAccountSessionDO entity) {
        // 直接从数据库获取原始数据，不使用任何缓存
        AppAccountSessionDO existingSession = baseMapper.selectById(entity.getId());

        if (existingSession == null) {
            log.warn("updateById -> 未找到原始会话, sessionId={}", entity.getId());
            return false;
        }

        boolean result = super.updateById(entity);

        if (result) {
            // 清除所有相关缓存
            evictAllCaches(entity.getId(), existingSession.getAccountId());
            log.warn("updateById -> 更新会话并清除缓存, sessionId={}, accountId={}, holdQty={}, holdAvePrice={}",
                    entity.getId(), existingSession.getAccountId(),
                    entity.getHoldQty(), entity.getHoldAvePrice());
        }
        return result;
    }

    @Override
    public AppAccountSessionDO getById(Serializable id) {
        // 先尝试从缓存获取
        Cache cache = cacheManager.getCache(SESSION_CACHE);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(id);
            if (valueWrapper != null) {
                AppAccountSessionDO cachedResult = (AppAccountSessionDO) valueWrapper.get();
                log.debug("getById -> 命中缓存, sessionId={}", id);
                return cachedResult;
            }
        }

        // 缓存未命中，查询数据库
        AppAccountSessionDO result = super.getById(id);

        // 存入缓存
        if (cache != null && result != null) {
            cache.put(id, result);
        }

        return result;
    }

    /**
     * 清除所有相关缓存
     */
    private void evictAllCaches(Long sessionId, Long accountId) {
        // 清除 sessionCache
        try {
            Cache sessionCache = cacheManager.getCache(SESSION_CACHE);
            if (sessionCache != null) {
                sessionCache.evict(sessionId);
                log.info("evictAllCaches -> 清除 sessionCache, sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("evictAllCaches -> 清除 sessionCache 失败, sessionId={}", sessionId, e);
        }

        // 清除 accountSessionListCache
        try {
            Cache accountCache = cacheManager.getCache(ACCOUNT_SESSION_LIST_CACHE);
            if (accountCache != null) {
                accountCache.evict(accountId);
                log.info("evictAllCaches -> 清除 accountSessionListCache, accountId={}", accountId);
            }
        } catch (Exception e) {
            log.error("evictAllCaches -> 清除 accountSessionListCache 失败, accountId={}", accountId, e);
        }
    }

}

