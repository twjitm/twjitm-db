package com.twjitm.db.entity;


import com.twjitm.db.common.annotation.EntitySave;
import com.twjitm.db.common.annotation.FieldSave;
import com.twjitm.db.common.annotation.MethodSaveProxy;

/**
 * Created by twjitm on 2017/4/5.
 */
@EntitySave
public class BaseStringIDEntity extends AbstractEntity<String> {

    @FieldSave
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @MethodSaveProxy(proxy = "id")
    @Override
    public void setId(String id) {
        this.id = id;
    }
}
