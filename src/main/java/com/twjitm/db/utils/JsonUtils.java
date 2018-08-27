package com.twjitm.db.utils;


import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * Created by twjitm on 2017/2/28.
 */
public class JsonUtils {

    /**
     * 获取json字符串
     * @param map
     * @return
     */
    public static String getJsonStr(Map<String, String> map){
        return JSON.toJSONString(map);
    }

    @SuppressWarnings("unchecked")
    public static Map<String,String> getMapFromJson(String json){
        return JSON.parseObject(json, Map.class);
    }

}

