package com.twjitm.db.sharding;

import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 17/3/6.
 */
@Service
public class CustomerContextHolder {

    private static  final ThreadLocal<String> contextHolder = new ThreadLocal<String>();


    public  static String getCustomerType() {
        return (String) contextHolder.get();
    }
    /**
     * 通过字符串选择数据源
     * @param customerType
     */
    public static void setCustomerType(String customerType) {
        contextHolder.set(customerType);
    }

}
