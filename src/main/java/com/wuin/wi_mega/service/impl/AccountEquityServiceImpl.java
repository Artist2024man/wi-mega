package com.wuin.wi_mega.service.impl;

import com.wuin.wi_mega.common.enums.ExchangeEnum;
import com.wuin.wi_mega.model.bo.AccountEquityAddBO;
import com.wuin.wi_mega.repository.*;
import com.wuin.wi_mega.repository.domain.*;
import com.wuin.wi_mega.service.AccountEquityService;
import com.wuin.wi_mega.common.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AccountEquityServiceImpl implements AccountEquityService {

    @Autowired
    private StatAccountEquityLogMinuteRepository accountEquityLogMinuteRepository;

    @Autowired
    private StatAccountEquityLogHourRepository accountEquityLogHourRepository;

    @Autowired
    private StatAccountEquityLogDayRepository accountEquityLogDayRepository;

    @Autowired
    private AppAccountRepository accountRepository;

    @Autowired
    private StatUserEquityLogDayRepository userEquityLogDayRepository;

    @Autowired
    private StatUserEquityLogHourRepository userEquityLogHourRepository;

    @Autowired
    private StatUserEquityLogMinuteRepository userEquityLogMinuteRepository;

    @Override
    public void addLog(AppAccountDO accountDO, AccountEquityAddBO addBO) {
        //保存日志
        this.addMinuteLog(accountDO, addBO.getEquity(), addBO.getTime());
        this.addHourLog(accountDO, addBO.getEquity(), addBO.getTime());
        this.addDayLog(accountDO, addBO.getEquity(), addBO.getTime());
    }

    @Override
    public void sync() {
        List<Long> userIds = accountRepository.listUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            log.info("sync -> 存在账户的用户数量为空，无需同步");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (Long userId : userIds) {
            for (ExchangeEnum value : ExchangeEnum.values()) {
                try {
                    this.syncMinutes(userId, value.name(), now);
                    this.syncHours(userId, value.name(), now);
                    this.syncDay(userId, value.name(), now);
                } catch (Throwable t) {
                    log.error("sync -> exception, userId:" + userId + ", value:" + value, t);
                }
            }
        }

    }

    private void syncMinutes(Long userId, String exchange, LocalDateTime syncTime) {
        Long timeLong = Long.parseLong(DateUtils.beauty(syncTime, "yyyyMMddHHmm"));
        BigDecimal equity = accountRepository.sumByUserId(userId, exchange);

        StatUserEquityLogMinuteDO minuteDO = userEquityLogMinuteRepository.getByTimeLong(userId, exchange, timeLong);
        if (null == minuteDO) {
            minuteDO = new StatUserEquityLogMinuteDO();
            minuteDO.setUserId(userId);
            minuteDO.setExchange(exchange);
            minuteDO.setTimeLong(timeLong);
            minuteDO.setEquity(equity);
            minuteDO.setCreateTime(LocalDateTime.now());
            minuteDO.setUpdateTime(LocalDateTime.now());
            userEquityLogMinuteRepository.save(minuteDO);
        } else {
            StatUserEquityLogMinuteDO update = new StatUserEquityLogMinuteDO();
            update.setId(minuteDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            userEquityLogMinuteRepository.updateById(update);
        }
    }

    private void syncHours(Long userId, String exchange, LocalDateTime syncTime) {
        Long timeLong = Long.parseLong(DateUtils.beauty(syncTime, "yyyyMMddHH"));
        BigDecimal equity = accountRepository.sumByUserId(userId, exchange);
        StatUserEquityLogHourDO hourDO = userEquityLogHourRepository.getByTimeLong(userId, exchange, timeLong);
        if (null == hourDO) {
            hourDO = new StatUserEquityLogHourDO();
            hourDO.setUserId(userId);
            hourDO.setExchange(exchange);
            hourDO.setTimeLong(timeLong);
            hourDO.setEquity(equity);
            hourDO.setCreateTime(LocalDateTime.now());
            hourDO.setUpdateTime(LocalDateTime.now());
            userEquityLogHourRepository.save(hourDO);
        } else {
            StatUserEquityLogHourDO update = new StatUserEquityLogHourDO();
            update.setId(hourDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            userEquityLogHourRepository.updateById(update);
        }
    }

    private void syncDay(Long userId, String exchange, LocalDateTime syncTime) {
        Long timeLong = Long.parseLong(DateUtils.beauty(syncTime, "yyyyMMdd"));
        BigDecimal equity = accountRepository.sumByUserId(userId, exchange);
        StatUserEquityLogDayDO dayDO = userEquityLogDayRepository.getByTimeLong(userId, exchange, timeLong);
        if (null == dayDO) {
            dayDO = new StatUserEquityLogDayDO();
            dayDO.setUserId(userId);
            dayDO.setExchange(exchange);
            dayDO.setTimeLong(timeLong);
            dayDO.setEquity(equity);
            dayDO.setCreateTime(LocalDateTime.now());
            dayDO.setUpdateTime(LocalDateTime.now());
            userEquityLogDayRepository.save(dayDO);
        } else {
            StatUserEquityLogDayDO update = new StatUserEquityLogDayDO();
            update.setId(dayDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            userEquityLogDayRepository.updateById(update);
        }
    }

    private void addMinuteLog(AppAccountDO accountDO, BigDecimal equity, LocalDateTime time) {
        Long timeLong = Long.parseLong(DateUtils.beauty(time, "yyyyMMddHHmm"));

        StatAccountEquityLogMinuteDO logDO = accountEquityLogMinuteRepository.getByAccountIdAndTime(accountDO.getId(), timeLong);
        if (null == logDO) {
            logDO = new StatAccountEquityLogMinuteDO();
            logDO.setUserId(accountDO.getUserId());
            logDO.setAccountId(accountDO.getId());
            logDO.setExchange(accountDO.getExchange());
            logDO.setTimeLong(timeLong);
            logDO.setEquity(equity);
            logDO.setCreateTime(LocalDateTime.now());
            logDO.setUpdateTime(LocalDateTime.now());
            accountEquityLogMinuteRepository.save(logDO);
        } else {
            StatAccountEquityLogMinuteDO update = new StatAccountEquityLogMinuteDO();
            update.setId(logDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            accountEquityLogMinuteRepository.updateById(update);
        }
    }

    private void addHourLog(AppAccountDO accountDO, BigDecimal equity, LocalDateTime time) {
        Long timeLong = Long.parseLong(DateUtils.beauty(time, "yyyyMMddHH"));
        StatAccountEquityLogHourDO logDO = accountEquityLogHourRepository.getByAccountIdAndTime(accountDO.getId(), timeLong);
        if (null == logDO) {
            logDO = new StatAccountEquityLogHourDO();
            logDO.setUserId(accountDO.getUserId());
            logDO.setAccountId(accountDO.getId());
            logDO.setExchange(accountDO.getExchange());
            logDO.setTimeLong(timeLong);
            logDO.setEquity(equity);
            logDO.setCreateTime(LocalDateTime.now());
            logDO.setUpdateTime(LocalDateTime.now());
            accountEquityLogHourRepository.save(logDO);
        } else {
            StatAccountEquityLogHourDO update = new StatAccountEquityLogHourDO();
            update.setId(logDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            accountEquityLogHourRepository.updateById(update);
        }
    }

    private void addDayLog(AppAccountDO accountDO, BigDecimal equity, LocalDateTime time) {
        Long timeLong = Long.parseLong(DateUtils.beauty(time, "yyyyMMdd"));
        StatAccountEquityLogDayDO logDO = accountEquityLogDayRepository.getByAccountIdAndTime(accountDO.getId(), timeLong);
        if (null == logDO) {
            logDO = new StatAccountEquityLogDayDO();
            logDO.setUserId(accountDO.getUserId());
            logDO.setAccountId(accountDO.getId());
            logDO.setExchange(accountDO.getExchange());
            logDO.setTimeLong(timeLong);
            logDO.setEquity(equity);
            logDO.setCreateTime(LocalDateTime.now());
            logDO.setUpdateTime(LocalDateTime.now());
            accountEquityLogDayRepository.save(logDO);
        } else {
            StatAccountEquityLogDayDO update = new StatAccountEquityLogDayDO();
            update.setId(logDO.getId());
            update.setEquity(equity);
            update.setUpdateTime(LocalDateTime.now());
            accountEquityLogDayRepository.updateById(update);
        }
    }

}
