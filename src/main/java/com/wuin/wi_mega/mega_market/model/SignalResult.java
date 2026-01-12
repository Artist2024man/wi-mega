package com.wuin.wi_mega.mega_market.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 策略信号结果
 */
@Data
@NoArgsConstructor
public class SignalResult {
    private Signal signal;
    private double price;
    private double strength;
    private String reason;
    private Map<String, Object> indicators = new LinkedHashMap<>();

    public SignalResult(Signal signal, String reason) {
        this.signal = signal;
        this.reason = reason;
    }

    public SignalResult(Signal signal, double price, double strength, String reason) {
        this.signal = signal;
        this.price = price;
        this.strength = strength;
        this.reason = reason;
    }

    public void addIndicator(String name, Object value) {
        indicators.put(name, value);
    }
}

