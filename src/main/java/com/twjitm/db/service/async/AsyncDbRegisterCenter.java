package com.twjitm.db.service.async;

import com.twjitm.db.common.DbServiceName;
import com.twjitm.db.common.Loggers;
import com.twjitm.db.common.enums.DbOperationEnum;
import com.twjitm.db.entity.AbstractEntity;
import com.twjitm.db.service.common.service.IDbService;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.redis.AsyncRedisKeyEnum;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.db.service.redis.RedisInterface;
import com.twjitm.db.service.redis.RedisListInterface;
import com.twjitm.db.utils.EntityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by twjitm on 17/4/6.
 * db异步注册数据通知中心
 *  先把同类型的玩家数据都放在一个集合里，来保证对一个玩家的操作是顺讯执行的
 *
 *  然后把玩家变动通知，放入到玩家身上。
 */
@Service
public class AsyncDbRegisterCenter implements IDbService {

    private Logger logger = Loggers.dbLogger;

    @Autowired
    private NettyRedisService redisService;

    private void asyncEntity(EntityService entityService, AsyncEntityWrapper asyncEntityWrapper, AbstractEntity entity){
        //计算处于那个db
        long selectId = entityService.getShardingId(entity);
        int dbSelectId = entityService.getEntityServiceShardingStrategy().getShardingDBTableIndexByUserId(selectId);

        //加入到异步更新队列
        String unionKey = null;
        if(entity instanceof RedisInterface){
            unionKey = ((RedisInterface) entity).getUnionKey();
        }else if(entity instanceof RedisListInterface){
            unionKey = ((RedisListInterface) entity).getShardingKey();
        }

        //必须先push再sadd
        String simapleClassName = entity.getClass().getSimpleName();
        String aysncUnionKey = simapleClassName + "#" + unionKey;
        redisService.rPushString(aysncUnionKey, asyncEntityWrapper.serialize());
        redisService.saddString(AsyncRedisKeyEnum.ASYNC_DB.getKey() + dbSelectId + "#" + entity.getClass().getSimpleName(), aysncUnionKey);
    }

    /**
     * 异步个体更新
     * @param entityService
     * @param dbOperationEnum
     * @param entity
     */
    public void asyncRegisterEntity(EntityService entityService, DbOperationEnum dbOperationEnum, AbstractEntity entity){
        AsyncEntityWrapper asyncEntityWrapper = null;
        if(dbOperationEnum.equals(DbOperationEnum.INSERT) || dbOperationEnum.equals(DbOperationEnum.DELETE)){
            Map<String, String> map = EntityUtils.getCacheValueMap(entity);
            asyncEntityWrapper = new AsyncEntityWrapper(dbOperationEnum, map);
        }else if(dbOperationEnum.equals(DbOperationEnum.UPDATE)){
            Map<String, String> map = EntityUtils.getProxyChangedCacheValueMap(entity);
            asyncEntityWrapper = new AsyncEntityWrapper(dbOperationEnum, map);
        }
        asyncEntity(entityService, asyncEntityWrapper, entity);
        if(logger.isDebugEnabled()) {
            logger.debug("async register entity " + entity.getClass().getSimpleName() + " id: " + entity.getId() + " userId:" + entity.getUserId());
        }
    }

    /**
     * 异步批量更新
     * @param entityService
     * @param dbOperationEnum
     * @param entitiyList
     */
    public void asyncBatchRegisterEntity(EntityService entityService, DbOperationEnum dbOperationEnum, List<AbstractEntity> entitiyList){
        AsyncEntityWrapper asyncEntityWrapper = null;
        if(entitiyList.size() > 0) {
            if (dbOperationEnum.equals(DbOperationEnum.INSERT_BATCH) || dbOperationEnum.equals(DbOperationEnum.DELETE_BATCH)) {
                List<Map<String, String>> paramList = new ArrayList<>();
                for (AbstractEntity entity : entitiyList) {
                    Map<String, String> map = EntityUtils.getCacheValueMap(entity);
                    paramList.add(map);
                    if (logger.isDebugEnabled()) {
                        logger.debug("async batch register entity " + entity.getClass().getSimpleName() + " id: " + entity.getId() + " userId:" + entity.getUserId());
                    }
                }
                asyncEntityWrapper = new AsyncEntityWrapper(dbOperationEnum, paramList);
            } else if (dbOperationEnum.equals(DbOperationEnum.UPDATE_BATCH)) {
                List<Map<String, String>> paramList = new ArrayList<>();
                for (AbstractEntity entity : entitiyList) {
                    Map<String, String> map = EntityUtils.getProxyChangedCacheValueMap(entity);
                    paramList.add(map);
                    if (logger.isDebugEnabled()) {
                        logger.debug("async batch register entity " + entity.getClass().getSimpleName() + " id: " + entity.getId() + " userId:" + entity.getUserId());
                    }
                }
                asyncEntityWrapper = new AsyncEntityWrapper(dbOperationEnum, paramList);
            }
            AbstractEntity entity = entitiyList.get(0);
            asyncEntity(entityService, asyncEntityWrapper, entity);
        } else {
            logger.debug("async batch register entity null dbOperationEnum is " + dbOperationEnum);
        }
    }


    @Override
    public String getDbServiceName() {
        return DbServiceName.asyncDbRegisterCenter;
    }

    @Override
    public void startup() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
