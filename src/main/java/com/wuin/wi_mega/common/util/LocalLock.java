package com.wuin.wi_mega.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LocalLock {

    // 用于存储 key -> Lock 映射
    private static final ConcurrentHashMap<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 获取锁（阻塞）
     *
     * @param key 业务唯一键
     */
    public static boolean lock(String key, long waitSeconds) {
        ReentrantLock lock = LOCK_MAP.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            boolean success = waitSeconds <= 0 ? lock.tryLock() : lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            log.info("LocalLock -> lock, lockKey={}, waitSeconds={}, result={}", key, waitSeconds, success);
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void unlock(String key) {
        LOCK_MAP.computeIfPresent(key, (k, lock) -> {
            log.info("LocalLock -> unlock, key={}", key);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            return (lock.isLocked() || lock.hasQueuedThreads()) ? lock : null;
        });
    }

    public static <T> T execute(Object key, Task<T> task) {
        if (!lock(key.toString(), 30)) {
            log.warn("Local execute -> lock fail, key={}", key);
            return null;
        }
        try {
            return task.doTask();
        } finally {
            unlock(key.toString());
        }
    }

    public static void executeWithoutResult(Object key, VoidTask task) {
        if (!lock(key.toString(), 10)) {
            log.warn("Local executeWithoutResult -> lock fail, key={}", key);
            return;
        }

        try {
            task.doTask();
        } finally {
            unlock(key.toString());
        }
    }

    public interface Task<T> {
        T doTask();
    }

    public interface VoidTask {
        void doTask();

    }
}
