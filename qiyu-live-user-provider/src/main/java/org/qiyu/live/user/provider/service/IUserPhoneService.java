package org.qiyu.live.user.provider.service;

import org.qiyu.live.user.interfaces.dto.UserLoginDTO;
import org.qiyu.live.user.interfaces.dto.UserPhoneDTO;

import java.util.List;

public interface IUserPhoneService {

    /**
     * 手机号注册
     *
     * @param phone
     * @return
    www.imooc.com
     */
    UserLoginDTO login(String phone);
    /**
     * 更具用户 id 查询手机信息
     *
     * @param userId
     * @return
     */
    List<UserPhoneDTO> queryByUserId(Long userId);
    /**
     * 根据手机号查询
     *
     * @param phone
     * @return
     */
    UserPhoneDTO queryByPhone(String phone);
}
