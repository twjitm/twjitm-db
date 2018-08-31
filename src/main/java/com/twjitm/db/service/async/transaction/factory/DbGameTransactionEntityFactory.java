package com.twjitm.db.service.async.transaction.factory;



import com.twjitm.db.service.async.transaction.entity.AsyncDBSaveTransactionEntity;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.proxy.EntityProxyFactory;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.transaction.service.redis.NettyTransactionRedisService;
import com.twjitm.transaction.transaction.enums.NettyTransactionEntityCause;
import com.twjitm.transaction.transaction.factory.NettyTransactionEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 2017/4/12.
 */
@Service
public class DbGameTransactionEntityFactory extends NettyTransactionEntityFactory {

    @Autowired
    private DbGameTransactionKeyFactory dbGameTransactionKeyFactory;

    @Autowired
    private EntityProxyFactory entityProxyFactory;

    public AsyncDBSaveTransactionEntity createAsyncDBSaveTransactionEntity(NettyTransactionEntityCause cause, NettyTransactionRedisService transactionRedisService, String redisKey, String union, EntityService entityService, NettyRedisService redisService){
        String key = dbGameTransactionKeyFactory.getPlayerTransactionEntityKey(cause, redisKey, union);
        AsyncDBSaveTransactionEntity asyncDBSaveTransactionEntity = new AsyncDBSaveTransactionEntity(
                cause, union, transactionRedisService,
                entityService, redisService, entityProxyFactory);
        return asyncDBSaveTransactionEntity;
    }

    public DbGameTransactionKeyFactory getDbGameTransactionKeyFactory() {
        return dbGameTransactionKeyFactory;
    }

    public void setDbGameTransactionKeyFactory(DbGameTransactionKeyFactory dbGameTransactionKeyFactory) {
        this.dbGameTransactionKeyFactory = dbGameTransactionKeyFactory;
    }
}
