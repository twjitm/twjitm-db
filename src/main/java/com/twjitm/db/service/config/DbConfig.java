package com.twjitm.db.service.config;

/**
 * @author EGLS0807 - [Created on 2018-08-09 18:20]
 * @company http://www.g2us.com/
 * @jdk java version "1.8.0_77"
 */
public class DbConfig {
    private String dbId;
    private String asyncDbOperationSaveWorkerSize;
    private String asyncDbOperationSelectWorkerSize;
    private String asyncOperationPackageName;

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getDbId() {
        return dbId;
    }

    public void setAsyncDbOperationSaveWorkerSize(String asyncDbOperationSaveWorkerSize) {
        this.asyncDbOperationSaveWorkerSize = asyncDbOperationSaveWorkerSize;
    }

    public String getAsyncDbOperationSaveWorkerSize() {
        return asyncDbOperationSaveWorkerSize;
    }

    public void setAsyncDbOperationSelectWorkerSize(String asyncDbOperationSelectWorkerSize) {
        this.asyncDbOperationSelectWorkerSize = asyncDbOperationSelectWorkerSize;
    }

    public String getAsyncDbOperationSelectWorkerSize() {
        return asyncDbOperationSelectWorkerSize;
    }

    public void setAsyncOperationPackageName(String asyncOperationPackageName) {
        this.asyncOperationPackageName = asyncOperationPackageName;
    }

    public String getAsyncOperationPackageName() {
        return asyncOperationPackageName;
    }
}
