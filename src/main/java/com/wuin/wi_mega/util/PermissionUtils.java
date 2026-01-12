package com.wuin.wi_mega.util;

import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.repository.domain.AppUserDO;

public class PermissionUtils {

    public static void checkPermission(AppUserDO userLogin, Long desUserId) {
        if (UserTypeEnum.ADMIN.equalByCode(userLogin.getStatus())) {
            return;
        }

        if (userLogin.getId().compareTo(desUserId) == 0) {
            return;
        }
        throw new APIRuntimeException(IResponseStatusMsg.APIEnum.USER_AUTH_FAIL);
    }

}
