package org.qiyu.live.user.interfaces;

import org.qiyu.live.user.interfaces.dto.UserLoginDTO;
import org.qiyu.live.user.interfaces.dto.UserPhoneDTO;

import java.util.List;

public interface IUserPhoneRpc {

    /**
     * 手机号登录
     *
     * @param phone
     * @return
     */
    UserLoginDTO login(String phone);
    /**
     * 更具用户 id 查询手机信息
     www.imooc.com
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
