// All rights reserved
package com.wuin.wi_mega.common.filter;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.cache.redis.UserAuthCache;
import com.wuin.wi_mega.common.enums.UserStatusEnum;
import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class AuthInterceptor implements Ordered {

    @Autowired
    private UserAuthCache adminAuthCache;

    @Around("@annotation(com.wuin.wi_mega.common.annotations.AuthRequired)")
    public Object beanAnnotatedAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuthRequired authRequired = method.getAnnotation(AuthRequired.class);
        validate(authRequired, AuthUtils.getToken());
        return joinPoint.proceed();
    }

    private void validate(AuthRequired authRequired, String token) {
        log.info("Validate -> user auth check, token={}", token);
        if (authRequired == null) {
            return;
        }
        if (StringUtils.isEmpty(token)) {
            if (authRequired.required()) {
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_EXPIRE);
            }
            log.error("admin_token is empty");
            return;
        }
        AppUserDO userLogin = adminAuthCache.get(token);

        if (null == userLogin) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_EXPIRE);
        }

        // 检查用户是否被禁用
        if (UserStatusEnum.FORBIDDEN.equalsByCode(userLogin.getStatus())) {
            // 清除缓存
            adminAuthCache.remove(token);
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_FORBIDDEN);
        }

        // 判断权限
        boolean hasPermission = true;
        UserTypeEnum[] userTypeArr = authRequired.userTypes();
        if (userTypeArr != null && userTypeArr.length > 0) {
            hasPermission = false;
            for (UserTypeEnum userTypeEnum : userTypeArr) {
                if (userTypeEnum.equalByCode(userLogin.getUserType())) {
                    hasPermission = true;
                    break;
                }
            }
        }
        if (!hasPermission) {
            log.error("user request over his permission , token:{}", JSON.toJSONString(userLogin));
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_AUTH_FAIL_ROOT);
        }
        AuthUtils.setUserLogin(userLogin);
        adminAuthCache.set(token, userLogin);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
