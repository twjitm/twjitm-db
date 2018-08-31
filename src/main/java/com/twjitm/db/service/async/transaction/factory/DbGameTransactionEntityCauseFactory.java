package com.twjitm.db.service.async.transaction.factory;

import com.twjitm.transaction.transaction.enums.NettyTransactionEntityCause;
import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 2017/4/12.
 */
@Service
public class DbGameTransactionEntityCauseFactory {

    public final NettyTransactionEntityCause asyncDbSave = new NettyTransactionEntityCause("asyncDbSave");

    public NettyTransactionEntityCause getAsyncDbSave() {
        return asyncDbSave;
    }
}
