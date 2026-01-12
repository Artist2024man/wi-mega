package com.wuin.wi_mega.service;

import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.repository.domain.AppUserDO;

public interface AppUserService {

    Long create(AppUserDO userLogin, UserCreateReqVO reqVO);

    void update(AppUserDO userLogin, UserUpdateReqVO reqVO);

    void changePwd(AppUserDO userLogin, UserChangePwdReqVO reqVO);

    AppUserDO getUserInfo(AppUserDO userLogin);

    AppUserDO login(UserLoginReqVO reqVO);

    void logout(AppUserDO userLogin);

    HistoryLineVO historyLine(AppUserDO userLogin, UserHistoryLineReqVO reqVO);

    PageResponseVO<AppUserResVO> list(AppUserDO userLogin, PageRequestVO<AppUserListReqVO> requestVO);

    /**
     * 删除用户
     * @param userLogin 当前登录用户
     * @param userId 要删除的用户ID
     */
    void delete(AppUserDO userLogin, Long userId);

}
