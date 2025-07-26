package org.qiyu.live.id.generate.provider.service.impl;

import jakarta.annotation.Resource;
import org.qiyu.live.id.generate.provider.dao.mapper.IdGenerateMapper;
import org.qiyu.live.id.generate.provider.dao.po.IdGeneratePO;
import org.qiyu.live.id.generate.provider.service.IdGenerateService;
import org.qiyu.live.id.generate.provider.service.bo.LocalSeqIdBO;
import org.qiyu.live.id.generate.provider.service.bo.LocalUnSeqIdBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IdGenerateServiceImpl implements IdGenerateService, InitializingBean {

    @Resource
    private IdGenerateMapper idGenerateMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerateServiceImpl.class);
    Map<Integer, LocalSeqIdBO> localSeqIdBOMap = new ConcurrentHashMap<>();
    Map<Integer, LocalUnSeqIdBO> localUnSeqIdBOMap = new ConcurrentHashMap<>();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 3, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("id-generate-thread-" + ThreadLocalRandom.current().nextInt(1000));
            return t;
        }
    });
    private static final float UPDATE_RATE = 0.75f;
    private static final int SEQ_TYPE = 1;
    private static Map<Integer, Semaphore> semaphoreMap = new ConcurrentHashMap<>();


    @Override
    public Long getUnSeqId(Integer code) {
        if (code == null) {
            LOGGER.error("[getSeqId] code is error, code is {}", code);
            return null;
        }
        LocalUnSeqIdBO localUnSeqIdBO = localUnSeqIdBOMap.get(code);
        if (localUnSeqIdBO == null) {
            LOGGER.error("[getUnSeqId] localUnSeqIdBO is null, code is {}", code);
            return null;
        }
        Long returnId = localUnSeqIdBO.getIdQueue().poll();
        if(returnId == null) {
            LOGGER.error("[getUnSeqId] returnId is null, code is {}", code);
            return null;
        }
        this.refreshLocalUnSeqId(localUnSeqIdBO);
        return returnId;
    }

    @Override
    public Long getSeqId(Integer code) {
        if (code == null) {
            LOGGER.error("[getSeqId] code is error, code is {}", code);
            return null;
        }
        LocalSeqIdBO localSeqIdBO = localSeqIdBOMap.get(code);
        if (localSeqIdBO == null) {
            LOGGER.error("[getSeqId] localSeqIdBO is null, code is {}", code);
            return null;
        }
        this.refreshLocalSeqId(localSeqIdBO);
        long returnId = localSeqIdBO.getCurrentNum().incrementAndGet();
        if(returnId > localSeqIdBO.getNextThreshold()) {
            LOGGER.error("[getSeqId] id is over limit, code is {}", code);
            return null;
        }
        return returnId;
    }

    /**
     * 刷新本地有序id段
     * @param localSeqIdBO
     */
    private void refreshLocalSeqId(LocalSeqIdBO localSeqIdBO) {
        long step = localSeqIdBO.getNextThreshold() - localSeqIdBO.getCurrentStart();
        if (localSeqIdBO.getCurrentNum().get() - localSeqIdBO.getCurrentStart() > step * UPDATE_RATE) {
            Semaphore semaphore = semaphoreMap.get(localSeqIdBO.getId());
            if(semaphore == null) {
                LOGGER.error("semaphore is null, id is {}", localSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if(acquireStatus) {
                LOGGER.info("进行有序id同步操作");
                // 异步进行同步id段操作
                threadPoolExecutor.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePO);
                        LOGGER.info("有序id同步操作完成");
                    } catch (Exception e) {
                        LOGGER.error("[refreshLocalSeqId] error is {}", e);
                    } finally {
                        semaphoreMap.get(localSeqIdBO.getId()).release();
                    }
                });
            }
        }
    }

    /**
     * 刷新本地无序id段
     * @param localUnSeqIdBO
     */
    private void refreshLocalUnSeqId(LocalUnSeqIdBO localUnSeqIdBO) {
        long begin = localUnSeqIdBO.getCurrentStart();
        long end = localUnSeqIdBO.getNextThreshold();
        long remainSize = localUnSeqIdBO.getIdQueue().size();
        if((end - begin) * 0.25 > remainSize) {
            Semaphore semaphore = semaphoreMap.get(localUnSeqIdBO.getId());
            if (semaphore == null) {
                LOGGER.error("semaphore is null, id is {}", localUnSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus) {
                threadPoolExecutor.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localUnSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePO);
                        LOGGER.info("无序id段同步完成，id is {}", localUnSeqIdBO.getId());
                    } catch (Exception e) {
                        LOGGER.error("[refreshLocalUnSeqId], error is {}", e);
                    } finally {
                        semaphoreMap.get(localUnSeqIdBO.getId()).release();
                    }
                });
            }
        }
    }

    // bean初始化的回调方法
    @Override
    public void afterPropertiesSet() throws Exception {
        List<IdGeneratePO> idGeneratePOS = idGenerateMapper.selectAll();
        for (IdGeneratePO idGeneratePO : idGeneratePOS) {
            LOGGER.info("服务刚启动，抢占新的id段");
            tryUpdateMySQLRecord(idGeneratePO);
            semaphoreMap.put(idGeneratePO.getId(), new Semaphore(1));
        }
    }

    /**
     * 更新mysql里面的分部署id的配置信息，占用相应的id段
     * 同步执行,很多的网络IO，性能较慢
     *
     * @param idGeneratePO
     */
    private void tryUpdateMySQLRecord(IdGeneratePO idGeneratePO) {
        int updateResult = idGenerateMapper.updateNewIdAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
        if (updateResult > 0) {
            localIdBOHandler(idGeneratePO);
            return;
        }
        // 重试进行更新
        for(int i = 0; i < 3; i++) {
            idGeneratePO = idGenerateMapper.selectById(idGeneratePO.getId());
            updateResult = idGenerateMapper.updateNewIdAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
            if(updateResult > 0) {
                localIdBOHandler(idGeneratePO);
                return;
            }
        }
        throw new RuntimeException("表id段占用失败，竞争过于激烈， id is " + idGeneratePO.getId());
    }

    /**
     * 专门处理如何将本地ID对象放入到Map中，并且初始化
     * @param idGeneratePO
     */
    private void localIdBOHandler(IdGeneratePO idGeneratePO) {
        long currentStart = idGeneratePO.getCurrentStart();
        long nextThreshold = idGeneratePO.getNextThreshold();
        long currentNum = currentStart;
        if(idGeneratePO.getIsSeq() == SEQ_TYPE) {
            LocalSeqIdBO localSeqIdBO = new LocalSeqIdBO();
            localSeqIdBO.setId(idGeneratePO.getId());
            AtomicLong atomicLong = new AtomicLong(currentNum);
            localSeqIdBO.setCurrentNum(atomicLong);
            localSeqIdBO.setCurrentStart(currentStart);
            localSeqIdBO.setNextThreshold(nextThreshold);
            localSeqIdBOMap.put(localSeqIdBO.getId(), localSeqIdBO);
        } else {
            LocalUnSeqIdBO localUnSeqIdBO = new LocalUnSeqIdBO();
            localUnSeqIdBO.setCurrentStart(currentStart);
            localUnSeqIdBO.setNextThreshold(nextThreshold);
            localUnSeqIdBO.setId(idGeneratePO.getId());
            long begin = localUnSeqIdBO.getCurrentStart();
            long end = localUnSeqIdBO.getNextThreshold();
            ArrayList<Long> idList = new ArrayList<Long>((int) (end - begin));
            for (long i = begin; i < end; i++) {
                idList.add(i);
            }
            // 将id段打乱
            Collections.shuffle(idList);
            ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<>();
            idQueue.addAll(idList);
            localUnSeqIdBO.setIdQueue(idQueue);
            localUnSeqIdBOMap.put(localUnSeqIdBO.getId(), localUnSeqIdBO);
        }
    }
}
