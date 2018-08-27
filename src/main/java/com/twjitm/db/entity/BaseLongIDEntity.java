package com.twjitm.db.entity;


import com.twjitm.db.common.annotation.EntitySave;
import com.twjitm.db.common.annotation.FieldSave;
import com.twjitm.db.common.annotation.MethodSaveProxy;

/**
 * Created by twjitm on 17/4/5.
 */
@EntitySave
public class BaseLongIDEntity extends AbstractEntity<Long> {

    @FieldSave
    private Long id;

    @Override
    public Long getId() {
        return id;
    }


    @MethodSaveProxy(proxy="id")
    public void setId(Long id) {
        this.id = id;
    }

}
