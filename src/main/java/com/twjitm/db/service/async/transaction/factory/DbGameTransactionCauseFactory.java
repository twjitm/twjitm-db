package com.twjitm.db.service.async.transaction.factory;

import com.twjitm.transaction.transaction.enums.NettyTransactionCause;
import org.springframework.stereotype.Service;

/**
 * Created by twjitm on 17/4/13.
 */
@Service
public class DbGameTransactionCauseFactory {
    public NettyTransactionCause asyncDbSave = new NettyTransactionCause("asyncDbSave");

    public NettyTransactionCause getAsyncDbSave() {
        return asyncDbSave;
    }
}
