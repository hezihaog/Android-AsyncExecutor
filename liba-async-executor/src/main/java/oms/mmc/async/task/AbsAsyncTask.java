package oms.mmc.async.task;

import oms.mmc.async.AsyncExecutor;

/**
 * Package: oms.mmc.async.task
 * FileName: AbsAsyncTask
 * Date: on 2018/3/4  下午7:30
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public abstract class AbsAsyncTask implements IAsyncTask {
    private int mSymbol;
    private AsyncExecutor.AsyncCallback<?, ?> mCallback;

    public AbsAsyncTask(int symbol, AsyncExecutor.AsyncCallback<?, ?> callback) {
        mSymbol = symbol;
        mCallback = callback;
    }

    public int getSymbol() {
        return mSymbol;
    }

    @Override
    public AsyncExecutor.AsyncCallback<?, ?> getCallback() {
        return mCallback;
    }
}