package com.wuin.wi_mega.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 多账户配置，通过 application.yml 配置或环境变量注入。
 * <p>
 * 示例：
 * binance:
 * accounts:
 * - name: main
 * apiKey: ${BINANCE_API_KEY_MAIN}
 * secretKey: ${BINANCE_SECRET_KEY_MAIN}
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "binance")
public class BinanceProperties {

    private List<String> symbols;
}

