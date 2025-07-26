package org.qiyu.live.account.provider.service.impl;

import jakarta.annotation.Resource;

import org.qiyu.live.account.provider.service.IAccountTokenService;
import org.qiyu.live.framework.redis.starter.key.AccountProviderCacheKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AccountTokenServiceImpl implements IAccountTokenService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private AccountProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = cacheKeyBuilder.buildUserLoginTokenKey(token);
        redisTemplate.opsForValue().set(key, String.valueOf(userId), 30, TimeUnit.DAYS);
        return token;
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        String redisKey = cacheKeyBuilder.buildUserLoginTokenKey(tokenKey);
        Integer userId =(Integer) redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId);
    }


}
