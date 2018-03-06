package oms.mmc.async.manager;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;

import oms.mmc.async.task.IAsyncTask;

/**
 * Package: oms.mmc.async
 * FileName: AbsTaskManager
 * Date: on 2018/3/4  下午8:29
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public abstract class AbsTaskManager<T extends IAsyncTask> implements Handler.Callback, ITaskManager<T> {
    private ExecutorService mThreadPool;
    private final Handler mMainHandler;

    public AbsTaskManager(ExecutorService threadPool) {
        mThreadPool = threadPool;
        mMainHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public boolean executeTask(int symbol, T task) {
        if (task != null && task.getCallback() != null && !task.getCallback().isStop()) {
            return true;
        }
        return false;
    }

    public ExecutorService getThreadPool() {
        return mThreadPool;
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }
}