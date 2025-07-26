package org.qiyu.live.user.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.account.interfaces.IAccountTokenRPC;
import org.qiyu.live.common.interfaces.enums.CommonStatusEnum;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.utils.DESUtils;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.id.generate.enums.IdTypeEnum;
import org.qiyu.live.id.generate.interfaces.IdGenerateRpc;
import org.qiyu.live.user.interfaces.dto.UserDTO;
import org.qiyu.live.user.interfaces.dto.UserLoginDTO;
import org.qiyu.live.user.interfaces.dto.UserPhoneDTO;
import org.qiyu.live.user.provider.dao.mapper.IUserPhoneMapper;
import org.qiyu.live.user.provider.dao.po.UserPhonePO;
import org.qiyu.live.user.provider.rpc.UserPhoneRpcImpl;
import org.qiyu.live.user.provider.service.IUserPhoneService;
import org.qiyu.live.user.provider.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserPhoneServiceImpl implements IUserPhoneService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPhoneRpcImpl.class);

    @Resource
    private IUserPhoneMapper userPhoneMapper;
    @Resource
    private IUserService userService;
    @DubboReference
    private IdGenerateRpc idGenerateRpc;
    @DubboReference
    private IAccountTokenRPC accountTokenRPC;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;


    @Override
    public UserLoginDTO login(String phone) {
        if (phone == null) {
            return UserLoginDTO.loginError("手机号不能为空");
        }
        UserPhoneDTO userPhoneDTO = queryByPhone(phone);
        if (userPhoneDTO != null) {
            LOGGER.error("phone is {} 已经被注册,可以直接登录", phone);
            return UserLoginDTO.loginSuccess(userPhoneDTO.getUserId(), accountTokenRPC.createAndSaveLoginToken(userPhoneDTO.getUserId()));
        }
        //进行注册操作
        return registerUser(phone);

    }
    private String createAndSaveToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = cacheKeyBuilder.buildUserLoginTokenKey(token);
        redisTemplate.opsForValue().set(tokenKey, userId, 30, TimeUnit.DAYS);
        return token;
    }
    private UserLoginDTO registerUser(String phone) {
        Long newUserId = idGenerateRpc.getUnSeqId(IdTypeEnum.USER_ID.getCode());
        //将手机号右移 2 位，然后取末尾两位数字
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(newUserId);
        userDTO.setNickName("旗鱼用户-" + userDTO.getUserId());
        userService.insertOne(userDTO);
        UserPhonePO userPhonePO = new UserPhonePO();
        userPhonePO.setPhone(DESUtils.encrypt(phone));
        userPhonePO.setUserId(newUserId);
        userPhonePO.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
        userPhoneMapper.insert(userPhonePO);
        // 删除缓存中的值
        redisTemplate.delete(cacheKeyBuilder.buildUserPhoneObjKey(phone));
        return UserLoginDTO.loginSuccess(newUserId, accountTokenRPC.createAndSaveLoginToken(newUserId));
    }


    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        if(userId == null || userId < 10_000) {
            return Collections.emptyList();
        }
        String redisKey = cacheKeyBuilder.buildUserPhoneListKey(userId);
        List<Object> userPhoneCacheList = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (!CollectionUtils.isEmpty(userPhoneCacheList)) {
            //可能是缓存的空值
            if (((UserPhoneDTO)userPhoneCacheList.get(0)).getUserId() == null) {
                return Collections.emptyList();
            }
            return userPhoneCacheList.stream().map(x -> (UserPhoneDTO) x).collect(Collectors.toList());
        }
        List<UserPhoneDTO> userPhoneDTOList = queryByUserIdFromDB(userId);
        if (!CollectionUtils.isEmpty(userPhoneDTOList)) {
            redisTemplate.opsForList().leftPushAll(redisKey, userPhoneDTOList.toArray());
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
            userPhoneDTOList.forEach(userPhoneDTO -> userPhoneDTO.setPhone(DESUtils.decrypt(userPhoneDTO.getPhone())));
            return userPhoneDTOList;
        }
        userPhoneDTOList = Arrays.asList(new UserPhoneDTO());
        redisTemplate.opsForList().leftPushAll(redisKey, userPhoneDTOList);
        redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);
        return userPhoneDTOList;
    }

    private List<UserPhoneDTO> queryByUserIdFromDB(Long userId) {
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getUserId, userId);
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convertList(userPhoneMapper.selectList(queryWrapper), UserPhoneDTO.class);
    }


    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        String redisKey = cacheKeyBuilder.buildUserPhoneObjKey(phone);
        UserPhoneDTO userPhoneDTO = (UserPhoneDTO) redisTemplate.opsForValue().get(redisKey);
        if (userPhoneDTO != null) {
            // 如果取出来的是空值缓存
            if(userPhoneDTO.getUserId() == null) {
                return null;
            }
            userPhoneDTO.setPhone(DESUtils.decrypt(userPhoneDTO.getPhone()));
            return userPhoneDTO;
        }
        userPhoneDTO = queryByPhoneFromDB(phone);
        if (userPhoneDTO != null) {
            redisTemplate.opsForValue().set(redisKey, userPhoneDTO, 30, TimeUnit.MINUTES);
            userPhoneDTO.setPhone(DESUtils.decrypt(userPhoneDTO.getPhone()));
            return userPhoneDTO;
        }
        // 缓存击穿(redis和mysql中都没有),布隆过滤器和空值缓存
        userPhoneDTO = new UserPhoneDTO();
        redisTemplate.opsForValue().set(redisKey, userPhoneDTO, 5, TimeUnit.MINUTES);
        return null;
    }


    public UserPhoneDTO queryByPhoneFromDB(String phone) {
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getPhone, DESUtils.encrypt(phone));
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(userPhoneMapper.selectOne(queryWrapper), UserPhoneDTO.class);
    }
}
