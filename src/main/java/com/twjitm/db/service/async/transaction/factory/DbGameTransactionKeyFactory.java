package com.twjitm.db.service.async.transaction.factory;


import com.twjitm.transaction.config.GlobalConstants;
import com.twjitm.transaction.transaction.enums.NettyTransactionEntityCause;
import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 2017/4/12.
 */
@Service
public class DbGameTransactionKeyFactory   {
    /**
     * 获取玩家锁
     * @param cause 事务实体产生的原因
     * @param redisKey redis key
     * @param union 联合key
     * @return
     */
    public String getPlayerTransactionEntityKey(NettyTransactionEntityCause cause, String redisKey,
                                                String union) {
        return redisKey + cause.getCause() + GlobalConstants.Strings.COMMON_SPLIT_STRING + union;
    }

}
