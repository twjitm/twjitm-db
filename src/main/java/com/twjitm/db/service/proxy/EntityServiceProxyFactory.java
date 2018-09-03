package com.twjitm.db.service.proxy;


import com.twjitm.db.service.entity.EntityService;
import com.twjitm.db.service.redis.NettyRedisService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by twjitm on 2017/3/23.
 * 实体存储服务代理服务
 */
@Service
public class EntityServiceProxyFactory {

    /**
     * db中的redis
     */
    @Resource
    private NettyRedisService redisService;


    @Autowired(required = false)
    private boolean useRedisFlag = true;

    private EntityServiceProxy createProxy(EntityService EntityService){
        return new EntityServiceProxy<>(redisService, useRedisFlag);
    }

    private <T extends  EntityService> T  createProxyService(T entityService, EntityServiceProxy entityServiceProxy){
        Enhancer enhancer = new Enhancer();
        //设置需要创建子类的类
        enhancer.setSuperclass(entityService.getClass());
        enhancer.setCallback(entityServiceProxy);
        //通过字节码技术动态创建子类实例
        return (T) enhancer.create();
    }

    public <T extends  EntityService> T createProxyService(T entityService) throws Exception {
        T proxyEntityService = (T) createProxyService(entityService, createProxy(entityService));
        BeanUtils.copyProperties(proxyEntityService,entityService);
        return proxyEntityService;
    }

    public NettyRedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(NettyRedisService redisService) {
        this.redisService = redisService;
    }
}
