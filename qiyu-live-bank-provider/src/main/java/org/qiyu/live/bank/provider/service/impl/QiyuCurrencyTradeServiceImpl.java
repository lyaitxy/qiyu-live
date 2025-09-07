package org.qiyu.live.bank.provider.service.impl;

import jakarta.annotation.Resource;
import org.qiyu.live.bank.provider.dao.mapper.QiyuCurrencyTradeMapper;
import org.qiyu.live.bank.provider.dao.po.QiyuCurrencyTradePO;
import org.qiyu.live.bank.provider.service.QiyuCurrencyTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QiyuCurrencyTradeServiceImpl implements QiyuCurrencyTradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QiyuCurrencyTradeServiceImpl.class);

    @Resource
    private QiyuCurrencyTradeMapper qiyuCurrencyTradeMapper;

    @Override
    public boolean insertOne(long userId, int num, int type) {

        try {
            QiyuCurrencyTradePO qiyuCurrencyTradePO = new QiyuCurrencyTradePO();
            qiyuCurrencyTradePO.setUserId(userId);
            qiyuCurrencyTradePO.setNum(num);
            qiyuCurrencyTradePO.setType(type);
            qiyuCurrencyTradeMapper.insert(qiyuCurrencyTradePO);
            return true;
        } catch (Exception e) {
            LOGGER.error("[QiyuCurrencyTradeServiceImpl] insert error is :", e);
        }
        return false;
    }
}
