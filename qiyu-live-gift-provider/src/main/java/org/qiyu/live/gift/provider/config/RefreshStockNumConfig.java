package org.qiyu.live.gift.provider.config;

import jakarta.annotation.Resource;
import org.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.gift.provider.service.IAnchorShopInfoService;
import org.qiyu.live.gift.provider.service.ISkuStockInfoService;
import org.qiyu.live.gift.rpc.ISkuStockInfoRpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class RefreshStockNumConfig {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;
    @Resource
    private ISkuStockInfoRpc skuStockInfoRpc;

    // 每15秒更新一次
    @Scheduled(cron = "*/15 * * * * ?")
    public void refreshStockNum() {
        String lockKey = cacheKeyBuilder.buildStockSyncLock();
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, 1, 15L, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isLock)) {
            List<Long> anchorIdList = anchorShopInfoService.queryAllValidAnchorId();
            for (Long anchorId : anchorIdList) {
                skuStockInfoRpc.syncStockNumToMySQL(anchorId);
            }
        }
    }
}
