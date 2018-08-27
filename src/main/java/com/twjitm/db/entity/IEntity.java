package com.twjitm.db.entity;

import java.io.Serializable;

/**
 * Created by twjitm on 17/3/16.
 * 基本的数据存储对象
 */
public interface IEntity<ID extends Serializable> extends Serializable {
    public ID getId();
    public void setId(ID id);
}


