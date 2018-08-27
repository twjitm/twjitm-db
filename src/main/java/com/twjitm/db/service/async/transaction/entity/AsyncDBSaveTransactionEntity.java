//package com.twjitm.db.service.async.transaction.entity;
//
//
//import com.twjitm.db.common.Loggers;
//import com.twjitm.db.common.enums.DbOperationEnum;
//import com.twjitm.db.entity.AbstractEntity;
//import com.twjitm.db.service.async.AsyncEntityWrapper;
//import com.twjitm.db.service.async.thread.AsyncDbOperationMonitor;
//import com.twjitm.db.service.entity.EntityService;
//import com.twjitm.db.service.proxy.EntityProxyFactory;
//import com.twjitm.db.service.redis.NettyRedisService;
//import com.twjitm.db.utils.EntityUtils;
//import com.twjitm.db.utils.ObjectUtils;
//import org.slf4j.Logger;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by twjitm on 2017/4/12.
// * 异步存储事务实体
// */
//public class AsyncDBSaveTransactionEntity  extends AbstractGameTransactionEntity {
//
//    private Logger logger = Loggers.dbErrorLogger;
//    /**
//     * db中redis服务
//     */
//    private NettyRedisService redisService;
//
//    /**
//     * 实体存储服务
//     */
//    private EntityService entityService;
//
//    /**
//     * 需要弹出的玩家key
//     */
//    private String playerKey;
//
//    private EntityProxyFactory entityProxyFactory;
//
//    private AsyncDbOperationMonitor asyncDbOperationMonitor;
//
//    public AsyncDBSaveTransactionEntity(GameTransactionEntityCause cause, String playerKey, IRGTRedisService irgtRedisService, EntityService entityService, NettyRedisService redisService
//                                        , EntityProxyFactory entityProxyFactory) {
//        super(cause, playerKey, irgtRedisService);
//        this.playerKey = playerKey;
//        this.entityService = entityService;
//        this.redisService = redisService;
//        this.entityProxyFactory = entityProxyFactory;
//    }
//
//    @Override
//    public void commit() throws GameTransactionException {
//        boolean startFlag = true;
//        do {
//            try {
//                String popKey = redisService.lpop(playerKey);
//                if(StringUtils.isEmpty(popKey)){
//                    break;
//                }
//
//                //开始保存数据库
//                AsyncEntityWrapper asyncEntityWrapper = new AsyncEntityWrapper();
//                asyncEntityWrapper.deserialize(popKey);
//                saveAsyncEntityWrapper(asyncEntityWrapper);
//            }catch (Exception e){
//                logger.error(e.toString(), e);
//                startFlag = false;
//            }
//
//
//        }while(startFlag);
//    }
//
//    private void saveAsyncEntityWrapper(AsyncEntityWrapper asyncEntityWrapper) throws Exception {
//        //开始进行反射，存储到mysql
//        Class targeClasses = entityService.getEntityTClass();
//        DbOperationEnum dbOperationEnum = asyncEntityWrapper.getDbOperationEnum();
//        if(dbOperationEnum.equals(DbOperationEnum.insert)){
//            AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targeClasses);
//            entityService.insertEntity(abstractEntity);
//        }else if(dbOperationEnum.equals(DbOperationEnum.delete)){
//            AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targeClasses);
//            entityService.deleteEntity(abstractEntity);
//            //TODO进行回调删除
//            EntityUtils.deleteEntity(redisService, abstractEntity);
//        }else if(dbOperationEnum.equals(DbOperationEnum.update)){
//            AbstractEntity abstractEntity = (AbstractEntity) targeClasses.newInstance();
//            abstractEntity =  entityProxyFactory.createProxyEntity(abstractEntity);
//            Map<String, String > changeStrings = asyncEntityWrapper.getParams();
//            ObjectUtils.getObjFromMap(changeStrings, abstractEntity);
//            entityService.updateEntity(abstractEntity);
//        }else if(dbOperationEnum.equals(DbOperationEnum.insertBatch)){
//            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
//            List<AbstractEntity> abstractEntityList = new ArrayList<>();
//            for(Map<String, String> temp: paramList){
//                AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targeClasses);
//                abstractEntityList.add(abstractEntity);
//            }
//            entityService.insertEntityBatch(abstractEntityList);
//        }else if(dbOperationEnum.equals(DbOperationEnum.updateBatch)){
//            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
//            List<AbstractEntity> abstractEntityList = new ArrayList<>();
//            for(Map<String, String> temp: paramList){
//                AbstractEntity abstractEntity = (AbstractEntity) targeClasses.newInstance();
//                abstractEntity =  entityProxyFactory.createProxyEntity(abstractEntity);
//                abstractEntityList.add(abstractEntity);
//            }
//            entityService.updateEntityBatch(abstractEntityList);
//        }else if(dbOperationEnum.equals(DbOperationEnum.deleteBatch)){
//            List<Map<String, String>> paramList = asyncEntityWrapper.getParamList();
//            List<AbstractEntity> abstractEntityList = new ArrayList<>();
//            for(Map<String, String> temp: paramList){
//                AbstractEntity abstractEntity = ObjectUtils.getObjFromMap(asyncEntityWrapper.getParams(), targeClasses);
//                abstractEntityList.add(abstractEntity);
//            }
//            entityService.deleteEntityBatch(abstractEntityList);
//            //TODO进行回调删除
//        }
//
//        asyncDbOperationMonitor.monitor();
//    }
//    @Override
//    public void rollback() throws GameTransactionException {
//
//    }
//
//    @Override
//    public GameTransactionCommitResult trycommit() throws GameTransactionException {
//        return GameTransactionCommitResult.SUCCESS;
//    }
//
//    public AsyncDbOperationMonitor getAsyncDbOperationMonitor() {
//        return asyncDbOperationMonitor;
//    }
//
//    public void setAsyncDbOperationMonitor(AsyncDbOperationMonitor asyncDbOperationMonitor) {
//        this.asyncDbOperationMonitor = asyncDbOperationMonitor;
//    }
//}
