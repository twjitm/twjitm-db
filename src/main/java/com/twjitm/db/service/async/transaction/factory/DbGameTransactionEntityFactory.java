package com.twjitm.db.service.async.transaction.factory;

import com.redis.transaction.enums.GameTransactionEntityCause;
import com.redis.transaction.factory.GameTransactionEntityFactory;
import com.redis.transaction.service.IRGTRedisService;
import com.snowcattle.game.db.service.async.transaction.entity.AsyncDBSaveTransactionEntity;

import com.twjitm.db.service.proxy.EntityProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 2017/4/12.
 */
@Service
public class DbGameTransactionEntityFactory extends GameTransactionEntityFactory {

    @Autowired
    private DbGameTransactionKeyFactory dbGameTransactionKeyFactory;

    @Autowired
    private EntityProxyFactory entityProxyFactory;
    public  AsyncDBSaveTransactionEntity createAsyncDBSaveTransactionEntity(GameTransactionEntityCause cause,IRGTRedisService irgtRedisService, String redisKey, String union, EntityService entityService, RedisService redisService){
        String key = dbGameTransactionKeyFactory.getPlayerTransactionEntityKey(cause, redisKey, union);
        AsyncDBSaveTransactionEntity asyncDBSaveTransactionEntity = new AsyncDBSaveTransactionEntity(cause, union, irgtRedisService, entityService, redisService, entityProxyFactory);
        return asyncDBSaveTransactionEntity;
    }

    public DbGameTransactionKeyFactory getDbGameTransactionKeyFactory() {
        return dbGameTransactionKeyFactory;
    }

    public void setDbGameTransactionKeyFactory(DbGameTransactionKeyFactory dbGameTransactionKeyFactory) {
        this.dbGameTransactionKeyFactory = dbGameTransactionKeyFactory;
    }
}
