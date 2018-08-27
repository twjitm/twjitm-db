package com.twjitm.db.service.config;

/**
 * @author EGLS0807 - [Created on 2018-08-09 18:20]
 * @company http://www.g2us.com/
 * @jdk java version "1.8.0_77"
 */
public class DbConfig {
    private String dbId;
    private int asyncDbOperationSaveWorkerSize;
    private int asyncDbOperationSelectWorkerSize;
    private String asyncOperationPackageName;

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getDbId() {
        return dbId;
    }

    public void setAsyncDbOperationSaveWorkerSize(int asyncDbOperationSaveWorkerSize) {
        this.asyncDbOperationSaveWorkerSize = asyncDbOperationSaveWorkerSize;
    }

    public int getAsyncDbOperationSaveWorkerSize() {
        return asyncDbOperationSaveWorkerSize;
    }

    public void setAsyncDbOperationSelectWorkerSize(int asyncDbOperationSelectWorkerSize) {
        this.asyncDbOperationSelectWorkerSize = asyncDbOperationSelectWorkerSize;
    }

    public int getAsyncDbOperationSelectWorkerSize() {
        return asyncDbOperationSelectWorkerSize;
    }

    public void setAsyncOperationPackageName(String asyncOperationPackageName) {
        this.asyncOperationPackageName = asyncOperationPackageName;
    }

    public String getAsyncOperationPackageName() {
        return asyncOperationPackageName;
    }
}
