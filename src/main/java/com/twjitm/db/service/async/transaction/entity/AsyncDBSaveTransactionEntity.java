package com.twjitm.db.service.async.transaction.entity;


import com.twjitm.db.common.Loggers;
import com.twjitm.db.common.enums.DbOperationEnum;
import com.twjitm.db.entity.AbstractEntity;
import com.twjitm.db.service.async.AsyncEntityWrapper;
import com.twjitm.db.service.async.thread.AsyncDbOperationMonitor;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.proxy.EntityProxyFactory;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.db.utils.EntityUtils;
import com.twjitm.db.utils.ObjectUtils;
import com.twjitm.transaction.service.redis.NettyTransactionRedisService;
import com.twjitm.transaction.transaction.entity.AbstractNettyTransactionEntity;
import com.twjitm.transaction.transaction.enums.NettyTransactionCommitResult;
import com.twjitm.transaction.transaction.enums.NettyTransactionEntityCause;
import com.twjitm.transaction.transaction.exception.NettyTransactionException;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * twjitm
 * 异步存储事务实体
 */
public class AsyncDBSaveTransactionEntity extends AbstractNettyTransactionEntity {

    private Logger logger = Loggers.dbErrorLogger;
    /**
     * db中redis服务
     */
    private NettyRedisService redisService;

    /**
     * 实体存储服务
     */
    private EntityService entityService;

    /**
     * 需要弹出的玩家key
     */
    private String playerKey;

    /**
     * 实体代理工厂
     */
    private EntityProxyFactory entityProxyFactory;

    /**
     * 异步操作监控器
     */
    private AsyncDbOperationMonitor asyncDbOperationMonitor;

    public AsyncDBSaveTransactionEntity(NettyTransactionEntityCause cause,
                                        String playerKey, NettyTransactionRedisService
                                                transactionRedisService,
                                        EntityService entityService
            , NettyRedisService redisService
            , EntityProxyFactory entityProxyFactory) {
        super(cause, playerKey, transactionRedisService);
        this.playerKey = playerKey;
        this.entityService = entityService;
        this.redisService = redisService;
        this.entityProxyFactory = entityProxyFactory;
    }

    @Override
    public void commit() throws NettyTransactionException {
        boolean startFlag = true;
        do {
            try {
                String popKey = redisService.lpop(playerKey);
                if (StringUtils.isEmpty(popKey)) {
                    break;
                }
                //开始保存数据库
                AsyncEntityWrapper asyncEntityWrapper = new AsyncEntityWrapper();
                asyncEntityWrapper.deserialize(popKey);
                saveAsyncEntityWrapper(asyncEntityWrapper);
            } catch (Exception e) {
                logger.error("提交保存数据");
                logger.error(e.toString(), e);
                startFlag = false;
            }

        } while (startFlag);
    }

    private void saveAsyncEntityWrapper(AsyncEntityWrapper asyncEntityWrapper) throws Exception {
        //开始进行反射，存储到mysql
        Class targetClasses = entityService.getEntityTClass();

        DbOperationEnum dbOperationEnum = asyncEntityWrapper.getDbOperationEnum();

        /**
         * TODO 为啥不要用switch case 语句。因为反射过程中不能使用
         */

        if (dbOperationEnum.equals(DbOperationEnum.INSERT)) {
            AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targetClasses);
            entityService.insertEntity(abstractEntity);
        } else if (dbOperationEnum.equals(DbOperationEnum.DELETE)) {
            AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targetClasses);
            entityService.deleteEntity(abstractEntity);
            //TODO 进行回调删除
            EntityUtils.deleteEntity(redisService, abstractEntity);
        } else if (dbOperationEnum.equals(DbOperationEnum.UPDATE)) {
            AbstractEntity abstractEntity = (AbstractEntity) targetClasses.newInstance();
            abstractEntity = entityProxyFactory.createProxyEntity(abstractEntity);
            Map<String, String> changeStrings = asyncEntityWrapper.getParams();
            ObjectUtils.getObjFromMap(changeStrings, abstractEntity);
            entityService.updateEntity(abstractEntity);
        } else if (dbOperationEnum.equals(DbOperationEnum.INSERT_BATCH)) {
            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
            List<AbstractEntity> abstractEntityList = new ArrayList<>();
            for (Map<String, String> temp : paramList) {
                AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targetClasses);
                abstractEntityList.add(abstractEntity);
            }
            entityService.insertEntityBatch(abstractEntityList);
        } else if (dbOperationEnum.equals(DbOperationEnum.UPDATE_BATCH)) {
            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
            List<AbstractEntity> abstractEntityList = new ArrayList<>();
            for (Map<String, String> temp : paramList) {
                AbstractEntity abstractEntity = (AbstractEntity) targetClasses.newInstance();
                abstractEntity = entityProxyFactory.createProxyEntity(abstractEntity);
                abstractEntityList.add(abstractEntity);
            }
            entityService.updateEntityBatch(abstractEntityList);
        } else if (dbOperationEnum.equals(DbOperationEnum.DELETE_BATCH)) {
            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
            List<AbstractEntity> abstractEntityList = new ArrayList<>();
            for (Map<String, String> temp : paramList) {
                AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targetClasses);
                abstractEntityList.add(abstractEntity);
            }
            entityService.deleteEntityBatch(abstractEntityList);
            //TODO进行回调删除
        }

        asyncDbOperationMonitor.monitor();
    }

    @Override
    public void rollback() throws NettyTransactionException {

    }

    @Override
    public NettyTransactionCommitResult tryCommit() throws NettyTransactionException {
        return NettyTransactionCommitResult.SUCCESS;
    }

    public AsyncDbOperationMonitor getAsyncDbOperationMonitor() {
        return asyncDbOperationMonitor;
    }

    public void setAsyncDbOperationMonitor(AsyncDbOperationMonitor asyncDbOperationMonitor) {
        this.asyncDbOperationMonitor = asyncDbOperationMonitor;
    }
}
