package oms.mmc.async.task;

import oms.mmc.async.AsyncExecutor;

/**
 * Package: oms.mmc.async.strategy
 * FileName: BindLifecycleTaskStrategy
 * Date: on 2018/3/4  下午6:27
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class BindLifecycleTask extends AbsAsyncTask {
    public BindLifecycleTask(int symbol, AsyncExecutor.AsyncCallback<?, ?> callback) {
        super(symbol, callback);
    }
}