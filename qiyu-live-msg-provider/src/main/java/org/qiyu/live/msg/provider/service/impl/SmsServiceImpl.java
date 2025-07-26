package org.qiyu.live.msg.provider.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.qiyu.live.framework.redis.starter.key.MsgProviderCacheKeyBuilder;
import org.qiyu.live.msg.dto.MsgCheckDTO;
import org.qiyu.live.msg.enums.MsgSendResultEnum;
import org.qiyu.live.msg.provider.config.ThreadPoolManager;
import org.qiyu.live.msg.provider.dao.mapper.SmsMapper;
import org.qiyu.live.msg.provider.dao.po.SmsPO;
import org.qiyu.live.msg.provider.service.ISmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements ISmsService {

    private static Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Resource
    private SmsMapper smsMapper;
    @Resource
    private RedisTemplate<String, Integer> redisTemplate;
    @Resource
    private MsgProviderCacheKeyBuilder redisKeyBuilder;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {

        //模拟产生一条短信记录
        int code = RandomUtils.nextInt(100000, 999999);
        String key = redisKeyBuilder.buildSmsLoginCodeKey(phone);
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            logger.error("短期内同一个手机号不能发送太多次，phone is {}", phone);
            //短期内同一个手机号不能发送太多次
            return MsgSendResultEnum.SEND_FAIL;
        }
        redisTemplate.opsForValue().set(key, code);
        redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        ThreadPoolManager.commonAsyncPool.execute(() -> {
            //模拟发送短信信息
            mockSendSms(phone, code);
            insertOne(phone, code);
        });
        return MsgSendResultEnum.SEND_SUCCESS;
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        String key = redisKeyBuilder.buildSmsLoginCodeKey(phone);
        Integer recordCode = redisTemplate.opsForValue().get(key);
        if (recordCode == null) {
            return new MsgCheckDTO(false, "验证码已失效");
        }
        boolean isCodeVerify = recordCode.equals(code);
        if (isCodeVerify) {
            redisTemplate.delete(key);
            return new MsgCheckDTO(true, "");
        }
        return new MsgCheckDTO(false, "验证码校验失败");
    }

    @Override
    public void insertOne(String phone, Integer code) {
        SmsPO smsPO = new SmsPO();
        smsPO.setPhone(phone);
        smsPO.setCode(code);
        smsMapper.insert(smsPO);
    }

    private void mockSendSms(String phone, Integer code) {
        try {
            logger.info(" ============= 创建短信发送通道中 ========= ==== ,phone is {},code is {}", phone, code);
            Thread.sleep(1000);
            logger.info(" ============= 短信已经发送成功 ========== === ");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
