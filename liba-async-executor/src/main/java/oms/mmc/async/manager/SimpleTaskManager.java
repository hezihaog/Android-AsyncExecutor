package oms.mmc.async.manager;

import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import oms.mmc.async.AsyncExecutor;
import oms.mmc.async.task.IAsyncTask;
import oms.mmc.async.task.SimpleTask;

/**
 * Package: oms.mmc.async
 * FileName: SimpleTaskManager
 * Date: on 2018/3/4  下午8:27
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class SimpleTaskManager extends AbsTaskManager<SimpleTask> {
    private ConcurrentHashMap<Integer, SimpleTask> callbackList;

    public SimpleTaskManager(ExecutorService threadPool) {
        super(threadPool);
        callbackList = new ConcurrentHashMap<Integer, SimpleTask>();
    }

    @Override
    public void addTask(int symbol, SimpleTask task) {
        if (callbackList.get(symbol) == null) {
            callbackList.put(symbol, task);
        }
    }

    @Override
    public void cancel(int symbol, AsyncExecutor.AsyncCallback callback) {
        if (callbackList.get(symbol) != null) {
            callbackList.remove(symbol);
            getMainHandler().removeMessages(symbol);
            callback.setStop(true);
            callback.onCancel(false);
        }
    }

    @Override
    public boolean executeTask(final int symbol, final SimpleTask task) {
        boolean isReady = super.executeTask(symbol, task);
        if (!isReady) {
            return false;
        }
        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                AsyncExecutor.AsyncCallback<?, ?> callback = task.getCallback();
                callback.setResult(callback.onRunning());
                getMainHandler().obtainMessage(symbol, task).sendToTarget();
            }
        });
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (getMainHandler().getLooper().getThread() == Thread.currentThread()) {
            if (msg.obj instanceof SimpleTask) {
                IAsyncTask task = (IAsyncTask) msg.obj;
                AsyncExecutor.AsyncCallback callback = task.getCallback();
                if (callback.isStop()) {
                    callback.onCancel(true);
                    return true;
                }
                callback.onRunAfter(callback.getResult());
                if (callbackList.containsKey(msg.what)) {
                    callbackList.remove(task);
                }
            }
        }
        return true;
    }
}