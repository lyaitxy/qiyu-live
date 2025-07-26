package org.qiyu.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.user.interfaces.constants.CacheAsyncDeleteCode;
import org.qiyu.live.user.interfaces.constants.UserProviderTopicNames;
import org.qiyu.live.user.interfaces.constants.UserTagFieldNameConstants;
import org.qiyu.live.user.interfaces.constants.UserTagsEnum;
import org.qiyu.live.user.interfaces.dto.UserCacheAsyncDeleteDTO;
import org.qiyu.live.user.interfaces.dto.UserTagDTO;
import org.qiyu.live.user.interfaces.utils.TagInfoUtils;
import org.qiyu.live.user.provider.dao.mapper.IUserTagMapper;
import org.qiyu.live.user.provider.dao.po.UserTagPO;
import org.qiyu.live.user.provider.service.IUserTagService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserTagServiceImpl implements IUserTagService {

    @Resource
    private IUserTagMapper userTagMapper;
    @Resource
    private RedisTemplate<String, UserTagDTO> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if(updateStatus) {
            // 这里已经修改成功了，进行redis删除
            deleteUserTagDTOFromRedis(userId);
            return true;
        }
        String key = userProviderCacheKeyBuilder.buildTagLockKey(userId);
        String setNxRes = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                return (String) connection.execute("set", keySerializer.serialize(key),
                        valueSerializer.serialize("-1"),
                        "NX".getBytes(StandardCharsets.UTF_8),
                        "EX".getBytes(StandardCharsets.UTF_8),
                        "3".getBytes(StandardCharsets.UTF_8));
            }
        });
        if(!"OK".equals(setNxRes)) {
            return false;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO != null) {
            return false;
        }
        userTagPO = new UserTagPO();
        userTagPO.setUserId(userId);
        userTagMapper.insert(userTagPO);
        updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        redisTemplate.delete(key);
        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        // 先尝试取消用户的标签
        boolean cancelStatus = userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (!cancelStatus) {
            return false;
        }
        // 从redis删除存入的缓存,更新后删除
        deleteUserTagDTOFromRedis(userId);
        return true;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDTO userTagDTO = this.queryByUserIdFromRedis(userId);
        if(userTagDTO == null) {
            return false;
        }
        String queryFieldName = userTagsEnum.getFieldName();
        if(queryFieldName.equals(UserTagFieldNameConstants.TAG_INFO_O1)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo01(), userTagsEnum.getTag());
        } else if (queryFieldName.equals(UserTagFieldNameConstants.TAG_INFO_O2)){
            return TagInfoUtils.isContain(userTagDTO.getTagInfo02(), userTagsEnum.getTag());
        } else if (queryFieldName.equals(UserTagFieldNameConstants.TAG_INFO_O3)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo03(), userTagsEnum.getTag());
        }
        return false;
    }

    /**
     * 刹车用户标签对象
     */
    private void deleteUserTagDTOFromRedis(Long userId) {
        String key = userProviderCacheKeyBuilder.buildTagKey(userId);
        redisTemplate.delete(key);
        UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
        userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode());
        Map<String, Object> jsonParam = new HashMap<>();
        jsonParam.put("userId", userId);
        userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));
        Message message = new Message();
        message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());
        message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
        // 延迟一秒进行缓存的删除
        message.setDelayTimeLevel(1);
        try{
            mqProducer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从redis中查询用户标签对象
     * @param userId
     * @return
     */
    private UserTagDTO queryByUserIdFromRedis(Long userId) {
        String key = userProviderCacheKeyBuilder.buildTagKey(userId);
        UserTagDTO userTagDTO = redisTemplate.opsForValue().get(key);
        if(userTagDTO != null) {
            return  userTagDTO;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if(userTagPO == null) {
            return null;
        }
        userTagDTO = ConvertBeanUtils.convert(userTagPO, UserTagDTO.class);
        redisTemplate.opsForValue().set(key, userTagDTO);
        return userTagDTO;
    }
}
