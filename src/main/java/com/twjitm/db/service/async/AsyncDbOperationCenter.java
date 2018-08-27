package com.twjitm.db.service.async;


import com.twjitm.db.common.DbServiceName;
import com.twjitm.db.service.async.thread.AsyncDbOperation;
import com.twjitm.db.service.async.thread.AsyncDbOperationMonitor;
import com.twjitm.db.service.common.service.IDbService;
import com.twjitm.db.service.config.DbConfig;
import com.twjitm.db.service.entity.AsyncOperationRegistry;
import com.twjitm.threads.common.executor.NettyUnorderThreadPollExecutor;
import com.twjitm.threads.utils.ExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by twjitm on 2017/4/12.
 */
@Service
public class AsyncDbOperationCenter implements IDbService {

    /**
     * 执行db落得第线程数量
     */
    private NettyUnorderThreadPollExecutor operationExecutor;

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private DbConfig dbConfig;

    @Autowired
    private AsyncOperationRegistry asyncOperationRegistry;

    @Override
    public String getDbServiceName() {
        return DbServiceName.asyncDbOperationCenter;
    }

    @Override
    public void startup() throws Exception {
        int coreSize =  dbConfig.getAsyncDbOperationSaveWorkerSize();
        String name = getDbServiceName();

//        operationExecutor = new NonOrderedQueuePoolExecutor(name, coreSize);
        int selectSize = dbConfig.getAsyncDbOperationSaveWorkerSize();
        scheduledExecutorService = Executors.newScheduledThreadPool(selectSize);

        //开始调度线程
        asyncOperationRegistry.startup();

        Collection<AsyncDbOperation> collection = asyncOperationRegistry.getAllAsyncEntityOperation();
        for(AsyncDbOperation asyncDbOperation: collection){
//            scheduledExecutorService.scheduleAtFixedRate(asyncDbOperation, 0, 60, TimeUnit.SECONDS);
            AsyncDbOperationMonitor asyncDbOperationMonitor = new AsyncDbOperationMonitor();
            asyncDbOperation.setAsyncDbOperationMonitor(asyncDbOperationMonitor);

            scheduledExecutorService.scheduleAtFixedRate(asyncDbOperation, 0, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void shutdown() throws Exception {
        if(scheduledExecutorService != null){
            ExecutorUtil.shutdownAndAwaitTermination(scheduledExecutorService, 60, TimeUnit.SECONDS);
        }
    }
}
