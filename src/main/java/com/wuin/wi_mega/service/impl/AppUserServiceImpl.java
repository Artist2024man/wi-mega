package com.wuin.wi_mega.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wuin.wi_mega.common.cache.redis.UserAuthCache;
import com.wuin.wi_mega.common.enums.UserStatusEnum;
import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.common.util.PasswordEncryptor;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.bo.AccountCountBO;
import com.wuin.wi_mega.model.bo.AccountEquityStatBO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.AppUserRepository;
import com.wuin.wi_mega.repository.StatUserEquityLogDayRepository;
import com.wuin.wi_mega.repository.StatUserEquityLogHourRepository;
import com.wuin.wi_mega.repository.StatUserEquityLogMinuteRepository;
import com.wuin.wi_mega.repository.domain.*;
import com.wuin.wi_mega.service.AppUserService;
import com.wuin.wi_mega.common.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserAuthCache userAuthCache;

    @Autowired
    private AppAccountRepository appAccountRepository;

    @Autowired
    private StatUserEquityLogMinuteRepository userEquityLogMinuteRepository;
    @Autowired
    private StatUserEquityLogHourRepository userEquityLogHourRepository;
    @Autowired
    private StatUserEquityLogDayRepository userEquityLogDayRepository;

    @Override
    public Long create(AppUserDO userLogin, UserCreateReqVO reqVO) {
        if (!UserTypeEnum.ADMIN.equalByCode(userLogin.getUserType())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_AUTH_FAIL);
        }
        AppUserDO userDO = appUserRepository.getByUsername(reqVO.getUsername());
        if (null != userDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USERNAME_EXIST);
        }
        AppUserDO user = new AppUserDO();
        user.setName(reqVO.getName());
        user.setUsername(reqVO.getUsername());
        user.setSalt(PasswordEncryptor.generateSalt());
        user.setPassword(PasswordEncryptor.hashPassword(reqVO.getPassword(), user.getSalt()));
        user.setUserType(UserTypeEnum.NORMAL.getCode());
        user.setStatus(UserStatusEnum.NORMAL.code());
        appUserRepository.save(user);
        return user.getId();
    }

    @Override
    public void update(AppUserDO userLogin, UserUpdateReqVO reqVO) {
        if (!UserTypeEnum.ADMIN.equalByCode(userLogin.getUserType())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_AUTH_FAIL);
        }
        AppUserDO userDO = appUserRepository.getById(reqVO.getId());
        if (null == userDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_NOT_EXIST);
        }
        AppUserDO update = new AppUserDO();
        update.setId(userDO.getId());
        update.setName(reqVO.getName());
        if (StringUtils.isNotBlank(reqVO.getPassword())) {
            update.setPassword(PasswordEncryptor.hashPassword(reqVO.getPassword(), userDO.getSalt()));
        }
        update.setStatus(reqVO.getStatus());
        appUserRepository.updateById(update);

        // 如果是禁用用户，需要清除该用户的登录缓存，使其立即失效
        if (reqVO.getStatus() != null && UserStatusEnum.FORBIDDEN.equalsByCode(reqVO.getStatus())) {
            userAuthCache.removeByUserId(userDO.getId());
            log.info("update -> 用户被禁用，已清除登录缓存, userId={}, username={}", userDO.getId(), userDO.getUsername());
        }
    }

    @Override
    public void changePwd(AppUserDO userLogin, UserChangePwdReqVO reqVO) {

        AppUserDO userDO = appUserRepository.getById(userLogin.getId());

        if (!PasswordEncryptor.verifyPassword(reqVO.getOldPwd(), userDO.getPassword(), userDO.getSalt())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PASSWORD_ERROR);
        }
        if (!reqVO.getNewPwd().equals(reqVO.getCheckPwd())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PASSWORD_NOT_EQUAL);
        }
        AppUserDO updateUser = new AppUserDO();
        updateUser.setId(userLogin.getId());
        updateUser.setPassword(PasswordEncryptor.hashPassword(reqVO.getNewPwd(), userDO.getSalt()));
        appUserRepository.updateById(updateUser);
    }

    @Override
    public AppUserDO getUserInfo(AppUserDO userLogin) {
        AppUserDO userDO = appUserRepository.getById(userLogin.getId());
        userDO.setPassword(null);
        userDO.setSalt(null);
        return userDO;
    }

    @Override
    public AppUserDO login(UserLoginReqVO reqVO) {
        AppUserDO userDO = appUserRepository.getByUsername(reqVO.getUsername());
        if (null == userDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USERNAME_NOT_EXIST);
        }
        // 检查用户是否被禁用
        if (UserStatusEnum.FORBIDDEN.equalsByCode(userDO.getStatus())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_FORBIDDEN);
        }
        if (!PasswordEncryptor.verifyPassword(reqVO.getPassword(), userDO.getPassword(), userDO.getSalt())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PASSWORD_ERROR);
        }
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        userDO.setToken(token);
        userAuthCache.set(token, userDO);
        userDO.setPassword(null);
        userDO.setSalt(null);
        return userDO;
    }

    @Override
    public void logout(AppUserDO userLogin) {
        userAuthCache.remove(userLogin.getToken());
    }

    @Override
    public HistoryLineVO historyLine(AppUserDO userLogin, UserHistoryLineReqVO reqVO) {

        long hours = Duration.between(reqVO.getStartTime(), reqVO.getEndTime()).toHours();

        HistoryLineVO lineVO = new HistoryLineVO();

        if (hours <= 8) {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMddHHmm"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMddHHmm"));
            List<StatUserEquityLogMinuteDO> list = userEquityLogMinuteRepository.listByTimeRange(userLogin.getId(), reqVO.getExchange() == null ? null : reqVO.getExchange().name(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatUserEquityLogMinuteDO minuteDO : list) {
                    lineVO.getLineX().add(minuteDO.getTimeLong().toString());
                    lineVO.getLineY().add(minuteDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        } else if (hours <= 336) {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMddHH"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMddHH"));
            List<StatUserEquityLogHourDO> list = userEquityLogHourRepository.listByTimeRange(userLogin.getId(), reqVO.getExchange() == null ? null : reqVO.getExchange().name(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatUserEquityLogHourDO dayDO : list) {
                    lineVO.getLineX().add(dayDO.getTimeLong().toString());
                    lineVO.getLineY().add(dayDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        } else {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMdd"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMdd"));
            List<StatUserEquityLogDayDO> list = userEquityLogDayRepository.listByTimeRange(userLogin.getId(), reqVO.getExchange() == null ? null : reqVO.getExchange().name(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatUserEquityLogDayDO dayDO : list) {
                    lineVO.getLineX().add(dayDO.getTimeLong().toString());
                    lineVO.getLineY().add(dayDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        return lineVO;

    }

    @Override
    public PageResponseVO<AppUserResVO> list(AppUserDO userLogin, PageRequestVO<AppUserListReqVO> requestVO) {

        IPage<AppUserDO> page = appUserRepository.pageList(requestVO.getParam(), requestVO.getPage(), requestVO.getPageSize());
        PageResponseVO<AppUserResVO> pageRes = new PageResponseVO<>();
        pageRes.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            List<AccountCountBO> countList = appUserRepository.statAccountCountByUserIds(page.getRecords().stream().map(AppUserDO::getId).collect(Collectors.toList()));
            Map<Long, AccountCountBO> countMap = CollectionUtils.isEmpty(countList) ? new HashMap<>() : countList.stream().collect(Collectors.toMap(AccountCountBO::getUserId, c -> c));
            List<AccountEquityStatBO> equityList = appUserRepository.statEquitySumByUserIds(page.getRecords().stream().map(AppUserDO::getId).collect(Collectors.toList()));
            Map<Long, AccountEquityStatBO> equityMap = CollectionUtils.isEmpty(equityList) ? new HashMap<>() : equityList.stream().collect(Collectors.toMap(AccountEquityStatBO::getUserId, c -> c));
            List<AppUserResVO> resList = new ArrayList<>();
            for (AppUserDO record : page.getRecords()) {
                resList.add(new AppUserResVO(record, countMap.get(record.getId()), equityMap.get(record.getId())));
            }
            pageRes.setRecords(resList);
        }
        return pageRes;
    }

    @Override
    public void delete(AppUserDO userLogin, Long userId) {
        // 仅管理员有权限删除用户
        if (!UserTypeEnum.ADMIN.equalByCode(userLogin.getUserType())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_AUTH_FAIL);
        }

        // 检查用户是否存在
        AppUserDO userDO = appUserRepository.getById(userId);
        if (null == userDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_NOT_EXIST);
        }

        // 不能删除管理员账号
        if (UserTypeEnum.ADMIN.equalByCode(userDO.getUserType())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.CANNOT_DELETE_ADMIN);
        }

        // 检查用户名下是否有绑定的账号
        long accountCount = appAccountRepository.countByUserId(userId);
        if (accountCount > 0) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_HAS_BINDIND_ACCOUNT);
        }

        // 清除用户登录缓存
        userAuthCache.removeByUserId(userId);

        // 执行删除
        appUserRepository.removeById(userId);
        log.info("delete user success, operatorId={}, deletedUserId={}, deletedUsername={}",
                userLogin.getId(), userId, userDO.getUsername());
    }
}
