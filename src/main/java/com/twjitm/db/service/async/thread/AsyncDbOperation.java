package com.twjitm.db.service.async.thread;


import com.twjitm.db.common.Loggers;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionCauseFactory;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionEntityCauseFactory;
import com.twjitm.db.service.async.transaction.factory.DbGameTransactionEntityFactory;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.proxy.EntityProxyFactory;
import com.twjitm.db.service.redis.AsyncRedisKeyEnum;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.db.sharding.EntityServiceShardingStrategy;

import com.twjitm.threads.common.executor.NettyUnorderThreadPollExecutor;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.TimerTask;

/**
 * Created by twjitm on 17/4/10.
 * 异步执行更新中心
 *  这个类采用模版编程
 */
@Service
public abstract class AsyncDbOperation<T extends EntityService> extends TimerTask {

    private Logger operationLogger = Loggers.dbLogger;
    /**
     * db里面的redis服务
     */
    @Autowired
    private NettyRedisService redisService;

   /* *//**
     * 事务redis服务
     *//*
    @Autowired
    private RGTRedisService rgtRedisService;*/

    /**
     * 事务服务
     */
   /* @Autowired
    private TransactionService transactionService;*/

    @Autowired
    private DbGameTransactionEntityFactory dbGameTransactionEntityFactory;

    @Autowired
    private DbGameTransactionEntityCauseFactory dbGameTransactionEntityCauseFactory;

    @Autowired
    private DbGameTransactionCauseFactory dbGameTransactionCauseFactory;

    @Autowired
    private EntityProxyFactory entityProxyFactory;

    /**
     * 执行db落得第线程数量
     */
    private NettyUnorderThreadPollExecutor operationExecutor;

    /**
     * 监视器
     */
    private AsyncDbOperationMonitor asyncDbOperationMonitor;

    public NettyUnorderThreadPollExecutor getOperationExecutor() {
        return operationExecutor;
    }

    public void setOperationExecutor(NettyUnorderThreadPollExecutor operationExecutor) {
        this.operationExecutor = operationExecutor;
    }

    @Override
    public void run() {
        if(operationLogger.isDebugEnabled()){
            operationLogger.debug("bootstrap async db operation");
        }
        asyncDbOperationMonitor.start();
        EntityService entityService = getWrapperEntityService();
        EntityServiceShardingStrategy entityServiceShardingStrategy = entityService.getEntityServiceShardingStrategy();
        int size = entityServiceShardingStrategy.getDbCount();
        for(int i = 0; i < size; i++){
            saveDb(i, entityService);
        }
        asyncDbOperationMonitor.printInfo(this.getClass().getSimpleName());
        asyncDbOperationMonitor.stop();
    }

    /**
     * 存储db
     * @param dbId
     * @param entityService
     */
    public void saveDb(int dbId, EntityService entityService){
        String simpleClassName = entityService.getEntityTClass().getSimpleName();
        String dbRedisKey = AsyncRedisKeyEnum.ASYNC_DB.getKey() + dbId + "#" + entityService.getEntityTClass().getSimpleName();
        long saveSize = redisService.scardString(dbRedisKey);
        for(long k = 0; k < saveSize; k++){
            String playerKey = redisService.spopString(dbRedisKey);
            if(StringUtils.isEmpty(playerKey)){
                break;
            }
            //@TODO hhah
            //如果性能不够的话，这里可以采用countdownlatch， 将下面逻辑进行封装，执行多线程更新

            //查找玩家数据进行存储 进行redis-game-transaction 加锁
           /* GameTransactionEntityCause gameTransactionEntityCause = dbGameTransactionEntityCauseFactory.getAsyncDbSave();
            AsyncDBSaveTransactionEntity asyncDBSaveTransactionEntity = dbGameTransactionEntityFactory.createAsyncDBSaveTransactionEntity(gameTransactionEntityCause, rgtRedisService, simpleClassName, playerKey, entityService, redisService);
            asyncDBSaveTransactionEntity.setAsyncDbOperationMonitor(asyncDbOperationMonitor);
            GameTransactionCommitResult commitResult = transactionService.commitTransaction(dbGameTransactionCauseFactory.getAsyncDbSave(), asyncDBSaveTransactionEntity);
            if(!commitResult.equals(GameTransactionCommitResult.SUCCESS)){
                //如果事务失败，说明没有权限禁行数据存储操作,需要放回去下次继续存储
                redisService.saddStrings(dbRedisKey, playerKey);
            }
            if(operationLogger.isDebugEnabled()) {
                operationLogger.debug("async save success" + playerKey);
            }*/
        }
    }

    //获取模版参数类
    public Class<T> getEntityTClass(){
        Class classes = getClass();
        Class result = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        return result;
    }

    public abstract EntityService getWrapperEntityService();

    public NettyRedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(NettyRedisService redisService) {
        this.redisService = redisService;
    }

    public DbGameTransactionEntityFactory getDbGameTransactionEntityFactory() {
        return dbGameTransactionEntityFactory;
    }

    public void setDbGameTransactionEntityFactory(DbGameTransactionEntityFactory dbGameTransactionEntityFactory) {
        this.dbGameTransactionEntityFactory = dbGameTransactionEntityFactory;
    }

    public DbGameTransactionEntityCauseFactory getDbGameTransactionEntityCauseFactory() {
        return dbGameTransactionEntityCauseFactory;
    }

    public void setDbGameTransactionEntityCauseFactory(DbGameTransactionEntityCauseFactory dbGameTransactionEntityCauseFactory) {
        this.dbGameTransactionEntityCauseFactory = dbGameTransactionEntityCauseFactory;
    }

   /* public RGTRedisService getRgtRedisService() {
        return rgtRedisService;
    }

    public void setRgtRedisService(RGTRedisService rgtRedisService) {
        this.rgtRedisService = rgtRedisService;
    }*/

    /*public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }*/

    public DbGameTransactionCauseFactory getDbGameTransactionCauseFactory() {
        return dbGameTransactionCauseFactory;
    }

    public void setDbGameTransactionCauseFactory(DbGameTransactionCauseFactory dbGameTransactionCauseFactory) {
        this.dbGameTransactionCauseFactory = dbGameTransactionCauseFactory;
    }


    public EntityProxyFactory getEntityProxyFactory() {
        return entityProxyFactory;
    }

    public void setEntityProxyFactory(EntityProxyFactory entityProxyFactory) {
        this.entityProxyFactory = entityProxyFactory;
    }

    public AsyncDbOperationMonitor getAsyncDbOperationMonitor() {
        return asyncDbOperationMonitor;
    }

    public void setAsyncDbOperationMonitor(AsyncDbOperationMonitor asyncDbOperationMonitor) {
        this.asyncDbOperationMonitor = asyncDbOperationMonitor;
    }
}
