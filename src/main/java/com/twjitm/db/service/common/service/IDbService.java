package com.twjitm.db.service.common.service;

/**
 * Created by twjitm on 17/4/17.
 */
public interface IDbService {
    public String getDbServiceName();
    public void startup() throws Exception;
    public void shutdown() throws Exception;
}
