package oms.mmc.async.task;

import oms.mmc.async.AsyncExecutor;

/**
 * Package: oms.mmc.async.strategy
 * FileName: SimpleTaskStrategy
 * Date: on 2018/3/4  下午6:24
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class SimpleTask extends AbsAsyncTask {
    public SimpleTask(int symbol, AsyncExecutor.AsyncCallback<?, ?> callback) {
        super(symbol, callback);
    }
}