// All rights reserved
package com.wuin.wi_mega.common.util;

import com.wuin.wi_mega.repository.domain.AppUserDO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthUtils {

    private static final String AUTH_HEADER = "wimegatoken";
    private static final ThreadLocal<AppUserDO> currentUser = new ThreadLocal<>();

    public static HttpServletRequest getRequest() {
        if(RequestContextHolder.getRequestAttributes() == null){
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void setUserLogin(AppUserDO userLogin) {
        currentUser.set(userLogin);
    }

    public static void remove() {
        currentUser.remove();
    }

    public static AppUserDO getUserLogin() {
        return currentUser.get();
    }

    public static String getToken() {
        return getToken(getRequest());
    }

    public static String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String headerToken = request.getHeader(AUTH_HEADER);
        if (StringUtils.isNotEmpty(headerToken)) {
            return headerToken;
        }

        return null;
    }

    /**
     * 获取客户机的ip地址
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
        }
        return ip;
    }
}
