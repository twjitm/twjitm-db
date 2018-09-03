package com.twjitm.db.service.async.thread;


import com.twjitm.db.common.Loggers;
import com.twjitm.db.service.async.transaction.entity.AsyncDBSaveTransactionEntity;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionCauseFactory;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionEntityCauseFactory;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionEntityFactory;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.redis.AsyncRedisKeyEnum;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.db.sharding.EntityServiceShardingStrategy;
import com.twjitm.transaction.service.redis.NettyTransactionRedisService;
import com.twjitm.transaction.service.transaction.NettyTransactionService;
import com.twjitm.transaction.transaction.enums.NettyTransactionCommitResult;
import com.twjitm.transaction.transaction.enums.NettyTransactionEntityCause;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.TimerTask;

/**
 * Created by twjitm on 17/4/10.
 * 异步执行更新中心
 * 这个类采用模版编程
 */
@Service
public abstract class AsyncDbOperation<T extends EntityService> extends TimerTask {

    private Logger operationLogger = Loggers.dbLogger;
    /**
     * db里面的redis服务
     */
    @Autowired
    private NettyRedisService redisService;

    /**
     * 事务redis服务:transaction 包提供的事务redis服务
     */
    @Resource
    private NettyTransactionRedisService rgtRedisService;

    /**
     * 事务服务
     */
    @Resource
    private NettyTransactionService transactionService;

    /**
     * 游戏事物实体工厂
     */
    @Autowired
    private DbGameTransactionEntityFactory dbGameTransactionEntityFactory;
    /**
     * 事务实体参数
     */
    @Autowired
    private DbGameTransactionEntityCauseFactory dbGameTransactionEntityCauseFactory;

    /**
     * 事务实体参数工厂
     */
    @Autowired
    private DbGameTransactionCauseFactory dbGameTransactionCauseFactory;

    /**
     * 监视器
     */
    @Resource
    private AsyncDbOperationMonitor asyncDbOperationMonitor;


    @Override
    public void run() {
        if (operationLogger.isDebugEnabled()) {
            operationLogger.debug("启动异步db操作");
        }
        asyncDbOperationMonitor.start();
        EntityService entityService = getWrapperEntityService();
        EntityServiceShardingStrategy entityServiceShardingStrategy = entityService.getEntityServiceShardingStrategy();
        int size = entityServiceShardingStrategy.getDbCount();
        for (int i = 0; i < size; i++) {
            asyncSaveDataToDb(i, entityService);
        }
        asyncDbOperationMonitor.printInfo(this.getClass().getSimpleName());
        asyncDbOperationMonitor.stop();
    }

    /**
     * 将保存在redis队列中的数据保存到关系型数据库中。存储db
     *
     * @param dbId
     * @param entityService
     */
    public void asyncSaveDataToDb(int dbId, EntityService entityService) {
        String simpleClassName = entityService.getEntityTClass().getSimpleName();
        String dbRedisKey = AsyncRedisKeyEnum.ASYNC_DB.getKey() + dbId + "#" + simpleClassName;
        //获取队列长度，
        long saveSize = redisService.scardString(dbRedisKey);

        //从队列里面把需要保存的数据取取出来
        for (long k = 0; k < saveSize; k++) {
            String playerKey = redisService.spopString(dbRedisKey);
            if (StringUtils.isEmpty(playerKey)) {
                break;
            }
            //如果性能不够的话，这里可以采用countdownlatch， 将下面逻辑进行封装，执行多线程更新

            //查找玩家数据进行存储 进行twjitm-transaction 加锁

            //事务实体产生的原因
            NettyTransactionEntityCause gameTransactionEntityCause = dbGameTransactionEntityCauseFactory
                    .getAsyncDbSave();

            //异步事务实体
            AsyncDBSaveTransactionEntity asyncDBSaveTransactionEntity =
                    dbGameTransactionEntityFactory.createAsyncDBSaveTransactionEntity(
                            gameTransactionEntityCause,
                            rgtRedisService,
                            simpleClassName,
                            playerKey,
                            entityService,
                            redisService);

            //设置监视器
            asyncDBSaveTransactionEntity.setAsyncDbOperationMonitor(asyncDbOperationMonitor);

            //提交事务
            NettyTransactionCommitResult commitResult = transactionService.commitTransaction
                    (dbGameTransactionCauseFactory.getAsyncDbSave(),
                            asyncDBSaveTransactionEntity);

            if (!commitResult.equals(NettyTransactionCommitResult.SUCCESS)) {
                //如果事务失败，说明没有权限禁行数据存储操作,需要放回去下次继续存储
                redisService.saddStrings(dbRedisKey, playerKey);
            }

            if (operationLogger.isDebugEnabled()) {
                operationLogger.debug("异步保存成功" + playerKey);
            }
        }
    }

    public abstract EntityService getWrapperEntityService();

    public void setAsyncDbOperationMonitor(AsyncDbOperationMonitor asyncDbOperationMonitor) {
        this.asyncDbOperationMonitor = asyncDbOperationMonitor;
    }
}
