package com.wuin.wi_mega.binance;

import com.wuin.wi_mega.binance.socket.MarketSocketClient;
import com.wuin.wi_mega.common.cache.local.DepthCacheFactory;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.constants.AppConstant;
import com.wuin.wi_mega.config.BinanceProperties;
import com.wuin.wi_mega.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MarkerSocketConfig implements InitializingBean {

    private static final String SK_ID = "MARKET_20251217_1831";

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private KlineCacheFactory klineCacheFactory;

    @Autowired
    private DepthCacheFactory depthCacheFactory;

    @Autowired
    private BinanceWebSocketRunner binanceWebSocketRunner;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> symbols = appConfigService.getList(AppConstant.CONFIG_SUPPORT_SYMBOL, String.class);
        if (CollectionUtils.isEmpty(symbols)) {
            log.warn("startKlineClient -> symbols is empty, can't start Kline Client...");
            return;
        }
        // 加入 socket 维护队列
        binanceWebSocketRunner.join(new MarketSocketClient(SK_ID, klineCacheFactory, depthCacheFactory, symbols));
    }
}
