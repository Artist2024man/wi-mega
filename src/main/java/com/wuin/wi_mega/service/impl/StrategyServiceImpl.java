package com.wuin.wi_mega.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wuin.wi_mega.binance.BinanceWebSocketRunner;
import com.wuin.wi_mega.binance.socket.UserDataSocketClient;
import com.wuin.wi_mega.common.enums.StrategyStatusEnum;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.common.registry.StrategyParamRegistry;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.queue.impl.UserDataQueue;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.AppStrategyInstanceRepository;
import com.wuin.wi_mega.repository.AppStrategyRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppStrategyDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import com.wuin.wi_mega.service.StrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wuin.wi_mega.common.enums.StrategyInstanceStatusEnum;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private BinanceWebSocketRunner binanceWebSocketRunner;
    @Autowired
    private UserDataQueue userDataQueue;
    @Autowired
    private AppStrategyRepository appStrategyRepository;
    @Autowired
    private AppStrategyInstanceRepository appStrategyInstanceRepository;
    @Autowired
    private AppAccountRepository appAccountRepository;

    @Override
    public void start(AppAccountDO accountDO) {
        Boolean isDualSidePosition = accountDO.fetchDualSidePosition();
        if (!isDualSidePosition) {
            String res = accountDO.changePositionMode(true);
            log.info("start -> 当前为单向持仓模式，修改模式为双向持仓,accountId={}, resp={}", accountDO.getId(), res);
        }
        UserDataSocketClient socketClient = new UserDataSocketClient(accountDO.getId().toString(), accountDO.getApiKey(), userDataQueue);
        binanceWebSocketRunner.join(socketClient);
    }

    @Override
    public void stop(AppAccountDO accountDO) {
//        binanceWebSocketRunner.leave(accountDO.getId().toString()); //停止用户数据
    }

    @Autowired
    private StrategyParamRegistry strategyParamRegistry;

    @Override
    public List<AppStrategyVO> listStrategy(AppUserDO userLogin) {
        List<AppStrategyDO> list = appStrategyRepository.list(new LambdaQueryWrapper<>());
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(strategyDO -> {
            AppStrategyVO vo = new AppStrategyVO(strategyDO);
            // 填充参数元数据
            vo.setParamMeta(
                    strategyParamRegistry.getBaseParamMeta(strategyDO.getCode()),
                    strategyParamRegistry.getRunParamMeta(strategyDO.getCode())
            );
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    public PageResponseVO<AppStrategyInstanceVO> listInstance(AppUserDO userLogin, PageRequestVO<AppStrategyInstanceListReqVO> requestVO) {
        AppStrategyInstanceListReqVO param = requestVO.getParam();
        if (param == null) {
            param = new AppStrategyInstanceListReqVO();
            requestVO.setParam(param);
        }
        param.setStatus(StrategyInstanceStatusEnum.ONLINE.getCode());

        IPage<AppStrategyInstanceDO> page = appStrategyInstanceRepository.pageList(
                param,
                requestVO.getPage(),
                requestVO.getPageSize()
        );

        PageResponseVO<AppStrategyInstanceVO> response = new PageResponseVO<>();
        response.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            response.setRecords(page.getRecords().stream()
                    .map(instanceDO -> {
                        AppStrategyInstanceVO vo = new AppStrategyInstanceVO(instanceDO);
                        // 填充键值对格式的参数列表
                        vo.setBaseParamList(
                                strategyParamRegistry.toParamKeyValueList(instanceDO.getCode(), vo.getBaseParam(), true)
                        );
                        vo.setRunParamList(
                                strategyParamRegistry.toParamKeyValueList(instanceDO.getCode(), vo.getRunParam(), false)
                        );
                        return vo;
                    })
                    .collect(Collectors.toList()));
        }
        return response;
    }


    @Override
    public PageResponseVO<AppStrategyInstanceVO> listInstanceForAdmin(AppUserDO userLogin, PageRequestVO<AppStrategyInstanceListReqVO> requestVO) {
        IPage<AppStrategyInstanceDO> page = appStrategyInstanceRepository.pageList(
                requestVO.getParam(),
                requestVO.getPage(),
                requestVO.getPageSize()
        );

        PageResponseVO<AppStrategyInstanceVO> response = new PageResponseVO<>();
        response.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            response.setRecords(page.getRecords().stream()
                    .map(instanceDO -> {
                        AppStrategyInstanceVO vo = new AppStrategyInstanceVO(instanceDO);
                        // 填充键值对格式的参数列表
                        vo.setBaseParamList(
                                strategyParamRegistry.toParamKeyValueList(instanceDO.getCode(), vo.getBaseParam(), true)
                        );
                        vo.setRunParamList(
                                strategyParamRegistry.toParamKeyValueList(instanceDO.getCode(), vo.getRunParam(), false)
                        );
                        return vo;
                    })
                    .collect(Collectors.toList()));
        }
        return response;
    }



    @Override
    public Long createInstance(AppUserDO userLogin, AppStrategyInstanceCreateReqVO requestVO) {

        AppStrategyDO strategyDO = appStrategyRepository.getById(requestVO.getStrategyId());

        if (null == strategyDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_NOT_EXIST);
        }

        List<String> supportSymbols = JSON.parseArray(strategyDO.getSymbols(), String.class);
        if (!supportSymbols.contains(requestVO.getSymbol())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_NOT_SUPPORT_SYMBOL);
        }

        AppStrategyInstanceDO instanceDO = new AppStrategyInstanceDO();
        instanceDO.setName(requestVO.getName());
        instanceDO.setStrategyId(strategyDO.getId());
        instanceDO.setCode(strategyDO.getCode());
        instanceDO.setExchange(requestVO.getExchange());
        instanceDO.setSymbol(requestVO.getSymbol());
        instanceDO.setBaseParam(JSON.toJSONString(requestVO.getBaseParam()));
        instanceDO.setRunParam(JSON.toJSONString(requestVO.getRunParam()));
        instanceDO.setRemark(requestVO.getRemark());
        // 默认下架状态
        instanceDO.setStatus(requestVO.getStatus() != null ? requestVO.getStatus() : StrategyInstanceStatusEnum.OFFLINE.getCode());
        appStrategyInstanceRepository.save(instanceDO);
        return instanceDO.getId();
    }

    @Override
    public void updateInstance(AppUserDO userLogin, AppStrategyInstanceCreateReqVO requestVO) {
        // 验证策略实例ID是否存在
        if (requestVO.getId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }

        AppStrategyInstanceDO existInstance = appStrategyInstanceRepository.getById(requestVO.getId());
        if (existInstance == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }

        // 验证策略模板是否存在
        AppStrategyDO strategyDO = appStrategyRepository.getById(requestVO.getStrategyId());
        if (null == strategyDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_NOT_EXIST);
        }

        // 验证交易对是否支持
        List<String> supportSymbols = JSON.parseArray(strategyDO.getSymbols(), String.class);
        if (!supportSymbols.contains(requestVO.getSymbol())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_NOT_SUPPORT_SYMBOL);
        }

        // 构建更新对象
        AppStrategyInstanceDO instanceDO = new AppStrategyInstanceDO();
        instanceDO.setId(requestVO.getId());
        instanceDO.setName(requestVO.getName());
        instanceDO.setStrategyId(strategyDO.getId());
        instanceDO.setCode(strategyDO.getCode());
        instanceDO.setExchange(requestVO.getExchange());
        instanceDO.setSymbol(requestVO.getSymbol());
        instanceDO.setBaseParam(JSON.toJSONString(requestVO.getBaseParam()));
        instanceDO.setRunParam(JSON.toJSONString(requestVO.getRunParam()));
        instanceDO.setRemark(requestVO.getRemark());
        // 更新状态
        if (requestVO.getStatus() != null) {
            instanceDO.setStatus(requestVO.getStatus());
        }

        appStrategyInstanceRepository.updateById(instanceDO);
    }

    @Override
    public void deleteInstance(AppUserDO userLogin, AppStrategyInstanceDeleteReqVO requestVO) {
        List<AppAccountDO> accountDOList = appAccountRepository.listByStrategyInstanceId(requestVO.getId(), StrategyStatusEnum.RUNNING.code());
        if (CollectionUtils.isNotEmpty(accountDOList)) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.HAS_RUNNING_ACCOUNTS);
        }
        appStrategyInstanceRepository.removeById(requestVO.getId());
    }

    @Override
    public void updateStatus(AppUserDO userLogin, AppStrategyInstanceStatusReqVO requestVO) {
        // 参数校验
        if (requestVO.getId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }
        if (requestVO.getStatus() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR);
        }

        // 验证策略实例是否存在
        AppStrategyInstanceDO existInstance = appStrategyInstanceRepository.getById(requestVO.getId());
        if (existInstance == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }

        // 更新状态
        AppStrategyInstanceDO updateDO = new AppStrategyInstanceDO();
        updateDO.setId(requestVO.getId());
        updateDO.setStatus(requestVO.getStatus());
        appStrategyInstanceRepository.updateById(updateDO);

        String statusName = StrategyInstanceStatusEnum.getNameByCode(requestVO.getStatus());
        log.info("updateStatus -> 策略状态更新成功, instanceId={}, status={}, statusName={}",
                requestVO.getId(), requestVO.getStatus(), statusName);
    }

}
