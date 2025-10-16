package org.qiyu.live.living.provider.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.qiyu.live.common.interfaces.dto.PageWrapper;
import org.qiyu.live.common.interfaces.enums.CommonStatusEnum;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.framework.redis.starter.key.LivingProviderCacheKeyBuilder;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.core.server.interfaces.dto.ImOfflineDTO;
import org.qiyu.live.im.core.server.interfaces.dto.ImOnlineDTO;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.im.router.interfaces.enums.ImMsgBizCodeEnum;
import org.qiyu.live.living.constants.LivingProviderTopicNames;
import org.qiyu.live.living.dto.LivingPKRespDTO;
import org.qiyu.live.living.dto.LivingRoomReqDTO;
import org.qiyu.live.living.dto.LivingRoomRespDTO;
import org.qiyu.live.living.provider.dao.mapper.LivingRoomMapper;
import org.qiyu.live.living.provider.dao.mapper.LivingRoomRecordMapper;
import org.qiyu.live.living.provider.dao.po.LivingRoomPO;
import org.qiyu.live.living.provider.dao.po.LivingRoomRecordPO;
import org.qiyu.live.living.provider.service.ILivingRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LivingRoomServiceImpl.class);

    @Resource
    private LivingRoomMapper livingRoomMapper;
    @Resource
    private LivingRoomRecordMapper livingRoomRecordMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private LivingProviderCacheKeyBuilder cacheKeyBuilder;
    @DubboReference
    private ImRouterRpc routerRpc;
    @Resource
    private MQProducer mqProducer;

    @Override
    public void userOnlineHandler(ImOnlineDTO imOnlineDTO) {
        Long userId = imOnlineDTO.getUserId();
        Integer appId = imOnlineDTO.getAppId();
        Integer roomId = imOnlineDTO.getRoomId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        redisTemplate.opsForSet().add(cacheKey, userId);
        redisTemplate.expire(cacheKey, 12L, TimeUnit.HOURS);
    }

    @Override
    public void userOfflineHandler(ImOfflineDTO imOfflineDTO) {
        Long userId = imOfflineDTO.getUserId();
        Integer appId = imOfflineDTO.getAppId();
        Integer roomId = imOfflineDTO.getRoomId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        redisTemplate.opsForSet().remove(cacheKey, userId);
        // 监听pk主播下线行为
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(imOfflineDTO.getRoomId());
        livingRoomReqDTO.setPkObjId(imOfflineDTO.getUserId());
        this.offlinePk(livingRoomReqDTO);
    }

    @Override
    public List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO) {
        Integer roomId = livingRoomReqDTO.getRoomId();
        Integer appId = livingRoomReqDTO.getAppId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        //使用scan分批查询数据，否则set元素太多容易造成redis和网络阻塞（scan会自动分成多次请求去执行）
        Cursor<Object> cursor = redisTemplate.opsForSet().scan(cacheKey, ScanOptions.scanOptions().match("*").count(100).build());
        List<Long> userIdList = new ArrayList<>();
        while (cursor.hasNext()) {
            userIdList.add((Long) cursor.next());
        }
        return userIdList;
    }

    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomPO livingRoomPO = ConvertBeanUtils.convert(livingRoomReqDTO, LivingRoomPO.class);
        livingRoomPO.setStartTime(new Date());
        livingRoomPO.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
        livingRoomMapper.insert(livingRoomPO);
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(livingRoomPO.getId());
        // 防止之前有空值缓存，这里做移除操作
        redisTemplate.delete(cacheKey);
        // 预热直播间商品的库存信息
        this.sendStartingLivingRoomMq(livingRoomPO);
        return livingRoomPO.getId();
    }

    private void sendStartingLivingRoomMq(LivingRoomPO livingRoomPO) {
        Message message = new Message();
        message.setBody(JSON.toJSONBytes(livingRoomPO));
        message.setTopic(LivingProviderTopicNames.START_LIVING_ROOM);
        try {
            mqProducer.send(message);
        } catch (Exception e) {
            LOGGER.error("[LivingRoomServiceImpl] starting living room and send mq error", e);
        }
    }

    @Override
    public boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomPO livingRoomPO = livingRoomMapper.selectById(livingRoomReqDTO.getRoomId());
        if(livingRoomPO == null) {
            return false;
        }
        if(!livingRoomReqDTO.getAnchorId().equals(livingRoomPO.getAnchorId())) {
            return false;
        }
        LivingRoomRecordPO livingRoomRecordPO = ConvertBeanUtils.convert(livingRoomPO, LivingRoomRecordPO.class);
        livingRoomRecordPO.setEndTime(new Date());
        livingRoomRecordPO.setStatus(CommonStatusEnum.INVALID_STATUS.getCode());
        livingRoomRecordMapper.insert(livingRoomRecordPO);
        livingRoomMapper.deleteById(livingRoomPO.getId());
        // 下播移除直播间缓存
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(livingRoomReqDTO.getRoomId());
        redisTemplate.delete(cacheKey);
        return true;
    }

    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        // 先查redis
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(roomId);
        LivingRoomRespDTO queryResult = (LivingRoomRespDTO) redisTemplate.opsForValue().get(cacheKey);
        if (queryResult != null) {
            // 防止空值缓存
            if (queryResult.getId() != null) {
                return null;
            }
            return queryResult;
        }
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getId, roomId);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        LivingRoomPO livingRoomPO = livingRoomMapper.selectOne(queryWrapper);
        queryResult = ConvertBeanUtils.convert(livingRoomPO, LivingRoomRespDTO.class);
        if(queryResult == null) {
            // 防止缓存穿透
            redisTemplate.opsForValue().set(cacheKey, new LivingRoomRespDTO(), 1L, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey, queryResult, 30, TimeUnit.MINUTES);
        return queryResult;
    }

    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO) {
        // 因为直播平台同时在线直播人数不多，属于读多写小的场景，所以将其缓存进Redis进行提速
        String cacheKey = cacheKeyBuilder.buildLivingRoomList(livingRoomReqDTO.getType());
        int page = livingRoomReqDTO.getPage();
        int pageSize = livingRoomReqDTO.getPageSize();
        Long total = redisTemplate.opsForList().size(cacheKey);
        List<Object> resultList = redisTemplate.opsForList().range(cacheKey, (long) (page - 1) * pageSize, (long) page * pageSize);
        PageWrapper<LivingRoomRespDTO> pageWrapper = new PageWrapper<>();
        if (CollectionUtils.isEmpty(resultList)) {
            pageWrapper.setList(Collections.emptyList());
            pageWrapper.setHasNext(false);
        } else {
            pageWrapper.setList(ConvertBeanUtils.convertList(resultList, LivingRoomRespDTO.class));
            pageWrapper.setHasNext((long) page * pageSize < total);
        }
        return pageWrapper;
    }

    @Override
    public List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getType, type);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        // 按照时间倒叙展示
        queryWrapper.orderByDesc(LivingRoomPO::getStartTime);
        // 为性能做保证，只查询1000个
        queryWrapper.last("limit 1000");
        return ConvertBeanUtils.convertList(livingRoomMapper.selectList(queryWrapper), LivingRoomRespDTO.class);
    }

    @Override
    public LivingPKRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomRespDTO livingRoomRespDTO = this.queryByRoomId(livingRoomReqDTO.getRoomId());
        LivingPKRespDTO respDTO = new LivingPKRespDTO();
        respDTO.setOnlineStatus(false);
        if (livingRoomRespDTO.getAnchorId().equals(livingRoomReqDTO.getPkObjId())) {
            respDTO.setMsg("主播不可以连线参与PK");
            return respDTO;
        }
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        // 使用setIfAbsent防止被后来者覆盖
        Boolean tryOnline = redisTemplate.opsForValue().setIfAbsent(cacheKey, livingRoomReqDTO.getPkObjId(), 12L, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(tryOnline)) {
            // 通知直播间所有人，有人上线PK了
            List<Long> userIdList = this.queryUserIdsByRoomId(livingRoomReqDTO);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pkObjId", livingRoomReqDTO.getPkObjId());
            jsonObject.put("pkObjAvatar", "../svga/img/爱心.png");
            this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_ONLINE.getCode(), jsonObject);
            respDTO.setMsg("连线成功");
            respDTO.setOnlineStatus(true);
            return respDTO;
        }
        respDTO.setMsg("目前有人在线，请稍后再试");
        return respDTO;
    }

    @Override
    public boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        return Boolean.TRUE.equals(redisTemplate.delete(cacheKey));
    }

    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(roomId);
        return (Long) redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 批量发送im消息
     */
    private void batchSendImMsg(List<Long> userIdList, Integer bizCode, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(bizCode);
            imMsgBody.setData(jsonObject.toJSONString());
            imMsgBody.setUserId(userId);
            return imMsgBody;
        }).collect(Collectors.toList());
        routerRpc.batchSendMsg(imMsgBodies);
    }

    @Override
    public LivingRoomRespDTO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getAnchorId, anchorId);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper), LivingRoomRespDTO.class);
    }
}
