package com.wuin.wi_mega.queue.impl;

import com.wuin.wi_mega.binance.bo.UserSocketDataEvent;
import com.wuin.wi_mega.queue.UniqueQueue;
import org.springframework.stereotype.Component;

@Component
public class UserDataQueue extends UniqueQueue<UserSocketDataEvent> {
}
