package com.twjitm.db.service.entity;


import com.twjitm.core.common.factory.classload.NettyClassloader;
import com.twjitm.core.common.utils.PackageScaner;
import com.twjitm.db.common.DbServiceName;
import com.twjitm.db.common.Loggers;
import com.twjitm.db.common.annotation.AsyncEntityOperation;
import com.twjitm.db.common.config.GlobalConstants;
import com.twjitm.db.service.async.thread.AsyncDbOperation;
import com.twjitm.db.service.common.service.IDbService;
import com.twjitm.db.service.config.DbConfig;
import com.twjitm.db.utils.BeanUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by twjitm on 17/4/11.
 * 异步服务注册中心
 */
@Service
public class AsyncOperationRegistry implements IDbService {

    public static Logger logger = Loggers.dbServerLogger;

    @Autowired
    private DbConfig dbConfig;

    BeanUtil beanUtil;

    /**
     * 包体扫描
     */
    public NettyClassloader messageScanner = new NettyClassloader();

    /**
     * 注册map
     */
    private ConcurrentHashMap<String, AsyncDbOperation> opeartionMap = new ConcurrentHashMap<String, AsyncDbOperation>();

    @Override
    public String getDbServiceName() {
        return DbServiceName.asyncOperationRegistry;
    }

    @Override
    public void startup() throws Exception {
        loadPackage(dbConfig.getAsyncOperationPackageName(),
                GlobalConstants.ClassConstants.Ext);
    }

    @Override
    public void shutdown() throws Exception {

    }


    public void loadPackage(String namespace, String ext) throws Exception {
        String[] fileNames = messageScanner.scannerPackage(namespace, ext);
        // 加载class,获取协议命令
        if (fileNames != null) {
            for (String fileName : fileNames) {
                String realClass = namespace
                        + "."
                        + fileName.subSequence(0, fileName.length()
                        - (ext.length()));
                Class<?> messageClass = Class.forName(realClass);

                logger.info("AsyncEntityOperation load:" + messageClass);
                AsyncEntityOperation asyncEntityOperation = messageClass.getAnnotation(AsyncEntityOperation.class);
                if (asyncEntityOperation != null) {
                    AsyncDbOperation asyncDbOperation = (AsyncDbOperation) beanUtil.getBean(asyncEntityOperation.bean());
                    opeartionMap.put(messageClass.getSimpleName(), asyncDbOperation);
                }
            }
        }
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public ConcurrentHashMap<String, AsyncDbOperation> getOpeartionMap() {
        return opeartionMap;
    }

    public void setOpeartionMap(ConcurrentHashMap<String, AsyncDbOperation> opeartionMap) {
        this.opeartionMap = opeartionMap;
    }

    public Collection<AsyncDbOperation> getAllAsyncEntityOperation() {
        return opeartionMap.values();
    }

    public BeanUtil getBeanUtil() {
        return beanUtil;
    }

    public void setBeanUtil(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }
}
