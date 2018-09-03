package com.twjitm.db.common.enums;

/**
 * Created by twjitm on 2017/3/23.
 * 数据存储操作
 */
public enum DbOperationEnum {
    /**
     * 插入操作
     */
    INSERT,
    /**
     * 更新操作
     */
    UPDATE,
    /**
     * 查询操作
     */
    QUERY,
    /**
     * 查询集和操作
     */
    QUERY_LIST,
    /**
     * 删除操作
     */
    DELETE,
    /**
     * 分批插入
     */
    INSERT_BATCH,
    /**
     * 分批更新
     */
    UPDATE_BATCH,
    /**
     * 分批删除
     */
    DELETE_BATCH,;
}
