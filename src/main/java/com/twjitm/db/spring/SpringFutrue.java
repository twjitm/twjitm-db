package com.twjitm.db.spring;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Service;import java.util.ArrayList;import java.util.List;/** * @author EGLS0807 - [Created on 2018-09-03 16:56] * @company http://www.g2us.com/ * @jdk java version "1.8.0_77" */@Servicepublic class SpringFutrue {    @Async    public  void addCallBack(AsyncTaskHandler asyncTaskHandler) {        int a = 0;        try {            Thread.sleep(1111);        } catch (InterruptedException e) {            e.printStackTrace();        }        asyncTaskHandler.success(a);        asyncTaskHandler.error(new NullPointerException());    }    @Async    public void getAllUser(AsyncTaskHandler asyncTaskHandler){        int a=0,b=1,c=3;        List<Integer> list=new ArrayList<>();        list.add(a);        list.add(b);        list.add(c);        asyncTaskHandler.success(list);        asyncTaskHandler.error(new NullPointerException());        return ;    }}