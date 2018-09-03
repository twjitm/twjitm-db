package com.twjitm.db.entity;


import com.twjitm.db.common.annotation.EntitySave;
import com.twjitm.db.common.annotation.FieldSave;
import com.twjitm.db.common.annotation.MethodSaveProxy;
import com.twjitm.db.service.entity.EntityKeyShardingStrategyEnum;
import com.twjitm.db.service.proxy.EntityProxyWrapper;
import com.twjitm.db.sharding.ShardingTable;

import java.io.Serializable;
import java.util.Date;

/**
 * 抽象事务实体，抽象事物实体为基础实体。
 * 游戏中的所有的数据库表对象都需要继承此对象。
 * @param <ID>
 */
@EntitySave
public abstract  class AbstractEntity<ID extends Serializable> extends ShardingTable implements ISoftDeleteEntity<ID>{

    @FieldSave
    private boolean deleted;

    @FieldSave
    private Date deleteTime;

//    @FieldSave
//    private ID id;

    @FieldSave
    private long userId;

    //用于记录数据库封装对象
    private EntityProxyWrapper entityProxyWrapper;

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    @MethodSaveProxy(proxy="deleted")
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public Date getDeleteTime() {
        return deleteTime;
    }

    @Override
    @MethodSaveProxy(proxy="deleteTime")
    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

//    @Override
//    public ID getId() {
//        return id;
//    }
//
//    @MethodSaveProxy(proxy="id")
//    public void setId(ID id) {
//        this.id = id;
//    }


    public long getUserId() {
        return userId;
    }

    @MethodSaveProxy(proxy="userId")
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public EntityProxyWrapper getEntityProxyWrapper() {
        return entityProxyWrapper;
    }

    public void setEntityProxyWrapper(EntityProxyWrapper entityProxyWrapper) {
        this.entityProxyWrapper = entityProxyWrapper;
    }

    public EntityKeyShardingStrategyEnum getEntityKeyShardingStrategyEnum(){
        return EntityKeyShardingStrategyEnum.USER_ID;
    }
}
