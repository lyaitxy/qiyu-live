package org.qiyu.live.bank.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.qiyu.live.bank.constants.OrderStatusEnum;
import org.qiyu.live.bank.constants.PayProductTypeEnum;
import org.qiyu.live.bank.constants.TradeTypeEnum;
import org.qiyu.live.bank.dto.PayOrderDTO;
import org.qiyu.live.bank.dto.PayProductDTO;
import org.qiyu.live.bank.interfaces.IQiyuCurrencyAccountRpc;
import org.qiyu.live.bank.provider.dao.mapper.PayOrderMapper;
import org.qiyu.live.bank.provider.dao.po.PayOrderPO;
import org.qiyu.live.bank.provider.dao.po.PayTopicPO;
import org.qiyu.live.bank.provider.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class PayOrderServiceImpl implements IPayOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayOrderServiceImpl.class);

    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private MQProducer mqProducer;
    @Resource
    private IPayTopicService payTopicService;
    @Resource
    private QiyuCurrencyAccountService qiyuCurrencyAccountService;
    @Resource
    private QiyuCurrencyTradeService qiyuCurrencyTradeService;
    @Resource
    private IPayProductService payProductService;
//    @Resource
//    private RedisSeqIdHelper redisSeqIdHelper;

    private static final String REDIS_ORDER_ID_INCR_KEY_PREFIX = "payOrderId";

    @Override
    public PayOrderPO queryByOrderId(String orderId) {
        LambdaQueryWrapper<PayOrderPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrderPO::getOrderId, orderId);
        queryWrapper.last("limit 1");
        return payOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public String insertOne(PayOrderPO payOrderPO) {
        // 生成一个订单号
//        String orderId = String.valueOf(redisSeqIdHelper.nextId(REDIS_ORDER_ID_INCR_KEY_PREFIX));
        String orderId = UUID.randomUUID().toString();
        payOrderPO.setOrderId(orderId);
        payOrderMapper.insert(payOrderPO);
        return payOrderPO.getOrderId();
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setId(id);
        payOrderPO.setStatus(status);
        return payOrderMapper.updateById(payOrderPO) > 0;
    }

    /**
     * 根据订单id更新状态
     * @param orderId
     * @param status
     * @return
     */
    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setStatus(status);
        LambdaUpdateWrapper<PayOrderPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrderPO::getOrderId, orderId);
        return payOrderMapper.update(payOrderPO, updateWrapper) > 0;
    }

    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        PayOrderPO payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        if (payOrderPO == null) {
            LOGGER.error("[PayOrderServiceImpl] payOrderPO is null, create a payOrderPO, userId is {}", payOrderDTO.getUserId());
//            qiyuCurrencyAccountService.insertOne(payOrderDTO.getUserId());
//            payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
            return false;
        }
        PayTopicPO payTopicPO = payTopicService.getByCode(payOrderDTO.getBizCode());
        if (payTopicPO == null || StringUtils.isEmpty(payTopicPO.getTopic())) {
            LOGGER.error("[PayOrderServiceImpl] error payTopicPO, payTopicPO is {}", payOrderDTO);
            return false;
        }

        this.payNotifyHandler(payOrderPO);


        // 假设 支付成功后，要发送消息通知 -》 msg-provider
        // 假设 支付成功后，要修改用户的VIP经验值
        // 使用mq
        // 中台服务，支付的对接方，10几种服务，pay-notify-topic

        Message message = new Message();
        message.setTopic(payTopicPO.getTopic());
        message.setBody(JSON.toJSONBytes(payOrderPO));
        SendResult sendResult = null;
        try {
            sendResult = mqProducer.send(message);
        } catch (Exception e) {
            LOGGER.error("[payNotify] sendResult is {}, error is ", sendResult, e);
        }
        return true;
    }

    private void payNotifyHandler(PayOrderPO payOrderPO) {
        // 订单状态更新
        this.updateOrderStatus(payOrderPO.getOrderId(), OrderStatusEnum.PAYED.getCode());
        Integer productId = payOrderPO.getProductId();
        PayProductDTO payProductDTO = payProductService.getByProductId(productId);
        if (payProductDTO != null && payProductDTO.getType().equals(PayProductTypeEnum.QIYU_COIN.getCode())) {
            // 类型是充值虚拟币业务：
            Long userId = payOrderPO.getUserId();
            JSONObject jsonObject = JSON.parseObject(payProductDTO.getExtra());
            Integer coinNum = jsonObject.getInteger("coin");
            qiyuCurrencyAccountService.incr(userId, coinNum);
        }
    }
}
