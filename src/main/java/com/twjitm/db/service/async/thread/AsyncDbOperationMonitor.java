package com.twjitm.db.service.async.thread;

import com.twjitm.db.common.Loggers;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by twjitm on 2017/4/18.
 * 监视器
 */
@Service
public class AsyncDbOperationMonitor{

    private Logger logger = Loggers.dbServerLogger;
    public AsyncDbOperationMonitor() {
        this.count = new AtomicLong();
    }

    public AtomicLong count;

//    public long startTime;

    private boolean totalFlag = true;
    public long startTime = System.currentTimeMillis();

    public void start(){
        if(!totalFlag){
            this.count.set(0);
            startTime = System.currentTimeMillis();
        }

    }
    public void monitor(){
        this.count.getAndIncrement();
    }

    public void stop()
    {
        if(!totalFlag) {
            this.count.set(0);
        }
    }

    public void printInfo(String opeartionName){
        long endTime = System.currentTimeMillis();
        long useTime = endTime - startTime;
        logger.debug("operation " + opeartionName + " count " + count.get() + "use time" + useTime);
    }

}
