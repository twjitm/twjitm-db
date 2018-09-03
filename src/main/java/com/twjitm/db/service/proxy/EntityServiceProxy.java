package com.twjitm.db.service.proxy;


import com.twjitm.db.common.Loggers;
import com.twjitm.db.common.annotation.DbOperation;
import com.twjitm.db.common.enums.DbOperationEnum;
import com.twjitm.db.entity.AbstractEntity;
import com.twjitm.db.entity.IEntity;
import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.redis.NettyRedisService;
import com.twjitm.db.service.redis.RedisInterface;
import com.twjitm.db.service.redis.RedisListInterface;
import com.twjitm.db.utils.EntityUtils;
import com.twjitm.db.utils.ObjectUtils;
import org.slf4j.Logger;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by twjitm on 2017/3/23.
 * 实体存储服务代理 同步存储
 *
 * 存储策略为 insert的时候插入db,然后更新缓存。query的时候优先缓存，找不到的时候查询db,更新缓存。delete的时候删除db，删除缓存，
 * useRedisFlag 为是否使用缓存redis标志
 */
public class EntityServiceProxy<T extends EntityService>  implements MethodInterceptor {

    private static final Logger proxyLogger = Loggers.dbServiceProxyLogger;

    private NettyRedisService redisService;

    private boolean useRedisFlag;

    public EntityServiceProxy(NettyRedisService redisService, boolean useRedisFlag) {
        this.redisService = redisService;
        this.useRedisFlag = useRedisFlag;
    }

    /**
     * spring 方法拦截
     * @param obj
     * @param method
     * @param args
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result = null;
        DbOperation dbOperation = method.getAnnotation(DbOperation.class);
        if(dbOperation == null || !useRedisFlag) { //如果没有进行注解或者不使用redis，直接进行返回
            result = methodProxy.invokeSuper(obj, args);
        }else {
            //进行数据库操作
            DbOperationEnum dbOperationEnum = dbOperation.operation();
            switch (dbOperationEnum) {
                case INSERT:
                    result = methodProxy.invokeSuper(obj, args);
                    AbstractEntity abstractEntity = (AbstractEntity) args[0];
                    EntityUtils.updateAllFieldEntity(redisService, abstractEntity);
                    break;
                case UPDATE:
                    result = methodProxy.invokeSuper(obj, args);
                    abstractEntity = (AbstractEntity) args[0];
                    EntityUtils.updateChangedFieldEntity(redisService, abstractEntity);
                    break;
                case QUERY:
                    abstractEntity = (AbstractEntity) args[0];
                    if (abstractEntity != null) {
                        if (abstractEntity instanceof RedisInterface) {
                            RedisInterface redisInterface = (RedisInterface) abstractEntity;
                            result = redisService.getObjectFromHash(EntityUtils.getRedisKey(redisInterface), abstractEntity.getClass());
                        } else {
                            proxyLogger.error("QUERY interface RedisListInterface " + abstractEntity.getClass().getSimpleName() + " use RedisInterface " + abstractEntity.toString());
                        }
                    }
                    if (result == null) {
                        result = methodProxy.invokeSuper(obj, args);
                        if(result != null){
                            abstractEntity = (AbstractEntity) result;
                            EntityUtils.updateAllFieldEntity(redisService, abstractEntity);
                        }
                    }
                    break;
                case QUERY_LIST:
                    abstractEntity = (AbstractEntity) args[0];
                    if (abstractEntity != null) {
                        if (abstractEntity instanceof RedisListInterface) {
                            RedisListInterface redisInterface = (RedisListInterface) abstractEntity;
                            result = redisService.getListFromHash(EntityUtils.getRedisKeyByRedisListInterface(redisInterface), abstractEntity.getClass());
                            if(result != null){
                                result = filterEntity((List<IEntity>) result, abstractEntity);
                            }
                        } else {
                            proxyLogger.error("QUERY interface RedisInterface " + abstractEntity.getClass().getSimpleName() + " use RedisListInterface " + abstractEntity.toString());
                        }
                    }
                    if (result == null) {
                        result = methodProxy.invokeSuper(obj, args);
                        if(result != null){
                            List<AbstractEntity> entityList = (List<AbstractEntity>) result;
                            EntityUtils.updateAllFieldEntityList(redisService, entityList);
                        }
                    }
                    break;
                case DELETE:
                    result = methodProxy.invokeSuper(obj, args);
                    abstractEntity = (AbstractEntity) args[0];
                    EntityUtils.deleteEntity(redisService, abstractEntity);
                    break;
                case INSERT_BATCH:
                    result = methodProxy.invokeSuper(obj, args);
                    List<AbstractEntity> entityList = (List<AbstractEntity>) args[0];
                    EntityUtils.updateAllFieldEntityList(redisService, entityList);
                    break;
                case UPDATE_BATCH:
                    result = methodProxy.invokeSuper(obj, args);
                    entityList = (List<AbstractEntity>) args[0];
                    EntityUtils.updateChangedFieldEntityList(redisService, entityList);
                    break;
                case DELETE_BATCH:
                    result = methodProxy.invokeSuper(obj, args);
                    entityList = (List<AbstractEntity>) args[0];
                    EntityUtils.deleteEntityList(redisService, entityList);
                    break;
            }
        }
        return result;
    }


    /**
     * 根据封装的条件判断查找出相同的对象
     * @param list
     * @param abstractEntity
     * @return
     */
    public List<IEntity> filterEntity(List<IEntity> list, AbstractEntity abstractEntity){
        List<IEntity> result = new ArrayList<>();
        //开始进行filter
        EntityProxyWrapper entityProxyWrapper = abstractEntity.getEntityProxyWrapper();
        if(entityProxyWrapper != null){
            Map<String, Object>  changeParamSet = entityProxyWrapper.getEntityProxy().getChangeParamSet();
            if(changeParamSet != null){
                for(IEntity iEntity: list) {
                    boolean equalFlag = false;
                    for (String fieldName : changeParamSet.keySet()) {
                        String value = ObjectUtils.getFieldsValueStr(iEntity, fieldName);

                        Object object = changeParamSet.get(fieldName);
                        if(value.equals(object.toString())){
                            equalFlag = true;
                        }else{
                            equalFlag = false;
                            break;
                        }
                    }

                    if(equalFlag){
                        result.add(iEntity);
                    }

                }
            }
        }
        return result;
    }
}
