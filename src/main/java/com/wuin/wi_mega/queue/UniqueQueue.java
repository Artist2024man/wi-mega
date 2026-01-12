package com.wuin.wi_mega.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class UniqueQueue<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<T, Boolean> inQueue = new ConcurrentHashMap<>();

    /**
     * 尝试入队
     *
     * @return true = 成功入队；false = 已存在于队列中
     */
    public void offer(T value) {
        if (inQueue.putIfAbsent(value, Boolean.TRUE) == null) {
            queue.offer(value);
        }
    }

    /**
     * 阻塞式出队
     */
    public T take() throws InterruptedException {
        T value = queue.take();
        inQueue.remove(value); // 释放占位，允许再次入队
        return value;
    }

    public T poll() {
        T value = queue.poll();
        if (value != null) {
            inQueue.remove(value); // 释放占位
        }
        return value;
    }

    public int size() {
        return queue.size();
    }
}
