package com.wuin.wi_mega.queue.handler;

import com.wuin.wi_mega.binance.bo.UserSocketDataEvent;
import com.wuin.wi_mega.queue.impl.UserDataQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BinanceUserDataHandler implements InitializingBean {

    @Autowired
    private UserDataQueue userDataQueue;

    private Thread thread;

    private static void handler(UserSocketDataEvent event) {
        log.warn("BinanceUserDataHandler -> handler:{}", event);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        thread = new Thread(() -> {
            log.info("BinanceUserDataHandler -> 用户数据处理线程...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    UserSocketDataEvent event = userDataQueue.take();
                    handler(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("BinanceUserDataHandler -> 用户数据处理异常", e);
                }
            }
        });
        thread.start();
    }
}
