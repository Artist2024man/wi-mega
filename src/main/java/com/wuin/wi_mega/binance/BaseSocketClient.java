package com.wuin.wi_mega.binance;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public abstract class BaseSocketClient {

    private LocalDateTime needRestartTime;

    @Getter
    private final String socketId;

    public BaseSocketClient(String socketId) {
        this.socketId = socketId;
    }

    protected abstract Long restartIntervalMinutes();

    public void start() {
        startInner();
        needRestartTime = LocalDateTime.now().plusMinutes(restartIntervalMinutes());
    }

    protected void restart() {
        restartInner();
        needRestartTime = LocalDateTime.now().plusMinutes(restartIntervalMinutes());
    }

    public void tryRestart() {
        if (null != needRestartTime && needRestartTime.isBefore(LocalDateTime.now())) {
            restart();
        }
    }

    protected abstract void startInner();

    protected abstract void restartInner();

    public abstract void close();

    public abstract Boolean isRunning();

    protected abstract void handlerMessage(String message);

}

