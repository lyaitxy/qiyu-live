package org.qiyu.live.bank.provider.service;

import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.dto.QiyuCurrencyAccountDTO;

public interface QiyuCurrencyAccountService {

    /**
     * 新增账户
     */
    boolean insertOne(Long userId);

    /**
     * 增加虚拟货币
     */
    void incr(Long userId, int num);

    /**
     * 扣减虚拟币
     */
    void decr(Long userId, int num);

    /**
     * 查询余额
     */
    Integer getBalance(Long userId);

    /**
     * 专门给送礼业务调用的扣减余额逻辑
     */
    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);

    /**
     * 底层需要判断用户余额是否充足，充足则扣减，不足则拦截
     */
    AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO);

}
