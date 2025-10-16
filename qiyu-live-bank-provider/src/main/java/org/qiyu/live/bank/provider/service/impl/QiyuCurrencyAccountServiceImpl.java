package org.qiyu.live.bank.provider.service.impl;

import jakarta.annotation.Resource;
import org.bouncycastle.asn1.x509.Time;
import org.qiyu.live.bank.constants.TradeTypeEnum;
import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.dto.QiyuCurrencyAccountDTO;
import org.qiyu.live.bank.provider.dao.mapper.QiyuCurrencyAccountMapper;
import org.qiyu.live.bank.provider.dao.po.QiyuCurrencyAccountPO;
import org.qiyu.live.bank.provider.service.QiyuCurrencyAccountService;
import org.qiyu.live.bank.provider.service.QiyuCurrencyTradeService;
import org.qiyu.live.common.interfaces.enums.CommonStatusEnum;
import org.qiyu.live.framework.redis.starter.key.BankProviderCacheKeyBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class QiyuCurrencyAccountServiceImpl implements QiyuCurrencyAccountService {

    @Resource
    private QiyuCurrencyAccountMapper qiyuCurrencyAccountMapper;
    @Resource
    private QiyuCurrencyTradeService qiyuCurrencyTradeService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));

    @Override
    public boolean insertOne(Long userId) {
        try {
            QiyuCurrencyAccountPO accountPO = new QiyuCurrencyAccountPO();
            accountPO.setUserId(userId);
            qiyuCurrencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
            //有异常但是不抛出，只为了避免重复创建相同userId的账户
        }
        return false;
    }

    @Override
    public void incr(Long userId, int num) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        // 如果Redis中存在缓存，基于Redis的余额增加
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            redisTemplate.opsForValue().increment(cacheKey, num);
        }
        // DB层操作（包括余额增加和流水记录）
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // 在异步线程池中完成数据库层的增加和流水记录，带有事务
                // 异步操作：CAP中的AP，没有追求强一致性，保证最终一致性即可（BASE理论）
                ((QiyuCurrencyAccountServiceImpl)AopContext.currentProxy()).incrDBHandler(userId, num);
            }
        });
    }

    // 增加旗鱼币的处理
    @Transactional(rollbackFor = Exception.class)
    public void incrDBHandler(Long userId, int num) {
        // 扣减余额(DB层)
        qiyuCurrencyAccountMapper.incr(userId, num);
        // 流水记录
        qiyuCurrencyTradeService.insertOne(userId, num, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    @Override
    public void decr(Long userId, int num) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        // 基于Redis的余额扣减
        redisTemplate.opsForValue().decrement(cacheKey, num);
        // DB层的操作（包括余额扣减和流水记录）
        threadPoolExecutor.execute(() ->{
            // 追求可用性，没有追求强一致性
            consumeDBHandler(userId, num);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void consumeDBHandler(Long userId, int num) {
        // 扣减余额（DB层）
        qiyuCurrencyAccountMapper.decr(userId, num);
        // 流水记录
        qiyuCurrencyTradeService.insertOne(userId, num * -1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    @Override
    public Integer getBalance(Long userId) {
        // 先查redis
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        Object cacheBalance = redisTemplate.opsForValue().get(cacheKey);
        if(cacheBalance != null) {
            if((Integer) cacheBalance == -1) {
                return null;
            }
            return (Integer) cacheBalance;
        }
        Integer balance = qiyuCurrencyAccountMapper.queryBalanceById(userId);
        if(balance == null) {
            redisTemplate.opsForValue().set(cacheKey, -1, 5, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey, balance, 30, TimeUnit.MINUTES);
        return balance;
    }

    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        // 余额判断
        long userId = accountTradeReqDTO.getUserId();
        int num = accountTradeReqDTO.getNum();
        Integer balance = this.getBalance(userId);
        if(balance == null || balance < num) {
            return AccountTradeRespDTO.buildFail(userId, "账户余额不足", 1);
        }
        this.decr(userId, num);
        return AccountTradeRespDTO.buildSuccess(userId, "扣费成功");
    }

    // 大并发请求场景，1000个直播间，500人，50W人在线，20%的人送礼，10W人在线触发送礼行为
    // DB扛不住
    // 1.MySQL换成写入性能相对较高的数据库
    // 2.我们能不能从业务上去进行优化，用户送礼都在直播间，大家都连接上了im服务器，router层扩容(50台)，im-core-server层(100台)，MQ削峰，消费端也可以水平扩容
    // 3.我们客户端发起送礼行为的时候，同步校验（校验账户余额是否足够，余额放入到Redis中）
    // 4.拦下大部分的请求，如果余额不足，（接口还得做防止重复点击，客户端也要放重复）
    // 5.同步送礼接口，只完成简单的余额校验，发送mq，在mq的异步操作里面，完成二次余额校验，余额扣减，礼物发送
    // 6.如果余额不足，是不是可以利用im，反向通知发送方，余额充足，利用im实现礼物特效推送
    @Override
    public AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO) {
        long userId = accountTradeReqDTO.getUserId();
//        // 首先判断账户余额是否充足，考虑无记录的情况
//        QiyuCurrencyAccountDTO accountDTO = this.getByUserId(userId);
//        if(accountDTO == null) {
//            return AccountTradeRespDTO.buildFail(userId, "账户还未初始化", 1);
//        }
//        if(!accountDTO.getStatus().equals(CommonStatusEnum.VALID_STATUS.getCode())) {
//            return AccountTradeRespDTO.buildFail(userId, "账号异常", 2);
//        }
//        if(accountDTO.getCurrentBalance() - accountTradeReqDTO.getNum() < 0) {
//            return AccountTradeRespDTO.buildFail(userId, "账户余额不足", 3);
//        }
//        // 流水记录
//        //
//        // 扣减余额
//        this.decr(userId, accountTradeReqDTO.getNum());
        return AccountTradeRespDTO.buildSuccess(userId, "扣费成功");
    }
}
