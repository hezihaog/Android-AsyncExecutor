package oms.mmc.async.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import oms.mmc.async.AsyncExecutor;
import oms.mmc.async.adapter.SimpleActivityLifecycle;
import oms.mmc.async.constant.LifecycleEvent;
import oms.mmc.async.task.BindLifecycleTask;
import oms.mmc.async.task.IAsyncTask;

/**
 * Package: oms.mmc.async
 * FileName: BindLIfecycleTaskManager
 * Date: on 2018/3/4  下午8:28
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class BindLifecycleTaskManager extends AbsTaskManager<BindLifecycleTask> {
    private final Object lock = new int[0];
    /**
     * 终止请求的生命周期类型
     */
    private LifecycleEvent mStopLifecycleEvent = LifecycleEvent.ON_DESTROY;
    /**
     * 回调集合
     */
    private ConcurrentHashMap<Integer, CopyOnWriteArrayList<BindLifecycleTask>> callbackList;
    /**
     * 生命周期回调监听器
     */
    private LifecycleCallback mLifecycleCallback;

    public BindLifecycleTaskManager(ExecutorService threadPool) {
        super(threadPool);
        callbackList = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<BindLifecycleTask>>();
        setStopOnLifecycleEvent(LifecycleEvent.ON_DESTROY);
    }

    /**
     * 注册生命周期监听
     */
    public void registerLifecycle(Application application) {
        if (mLifecycleCallback == null) {
            synchronized (lock) {
                if (mLifecycleCallback == null) {
                    mLifecycleCallback = new LifecycleCallback();
                    application.registerActivityLifecycleCallbacks(mLifecycleCallback);
                }
            }
        }
    }

    private class LifecycleCallback extends SimpleActivityLifecycle {
        @Override
        public void onActivityStarted(Activity activity) {
            checkStopLifecycleEvent(activity, LifecycleEvent.ON_START);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            checkStopLifecycleEvent(activity, LifecycleEvent.ON_RESUME);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            checkStopLifecycleEvent(activity, LifecycleEvent.ON_PAUSE);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            checkStopLifecycleEvent(activity, LifecycleEvent.ON_STOP);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            checkStopLifecycleEvent(activity, LifecycleEvent.ON_DESTROY);
        }
    }

    /**
     * 检查当前回调的生命周期是否是停止的任务的生命周期
     */
    private void checkStopLifecycleEvent(Activity activity, LifecycleEvent event) {
        if (mStopLifecycleEvent == event) {
            if (callbackList.containsKey(activity.hashCode())) {
                getMainHandler().removeMessages(activity.hashCode());
                List<BindLifecycleTask> list = callbackList.get(activity.hashCode());
                for (BindLifecycleTask task : list) {
                    if (task.getCallback() != null) {
                        task.getCallback().setStop(true);
                    }
                }
                callbackList.remove(activity.hashCode());
            }
        }
    }

    public void setStopOnLifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (lifecycleEvent.ordinal() == LifecycleEvent.ON_START.ordinal()
                || lifecycleEvent.ordinal() == LifecycleEvent.ON_RESUME.ordinal()
                || lifecycleEvent.ordinal() == LifecycleEvent.ON_PAUSE.ordinal()
                || lifecycleEvent.ordinal() == LifecycleEvent.ON_STOP.ordinal()
                || lifecycleEvent.ordinal() == LifecycleEvent.ON_DESTROY.ordinal()) {
            this.mStopLifecycleEvent = lifecycleEvent;
        }
    }

    @Override
    public void addTask(int symbol, BindLifecycleTask task) {
        CopyOnWriteArrayList<BindLifecycleTask> list = callbackList.get(symbol);
        if (list == null) {
            list = new CopyOnWriteArrayList<BindLifecycleTask>();
            callbackList.put(symbol, list);
        }
        if (!list.contains(task)) {
            list.add(task);
        }
    }

    @Override
    public void cancel(int symbol, AsyncExecutor.AsyncCallback callback) {
        if (callbackList.get(symbol) != null) {
            List<BindLifecycleTask> tasks = callbackList.get(symbol);
            if (tasks != null && tasks.size() > 0) {
                for (BindLifecycleTask task : tasks) {
                    if (task.getCallback().hashCode() == callback.hashCode()) {
                        tasks.remove(task);
                        getMainHandler().removeMessages(symbol, task);
                        task.getCallback().setStop(true);
                        callback.onCancel(false);
                    }
                }
            }
        }
    }


    @Override
    public boolean executeTask(final int symbol, final BindLifecycleTask task) {
        boolean isReady = super.executeTask(symbol, task);
        if (!isReady) {
            return false;
        }
        //在子线程执行任务
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
            if (msg.obj instanceof BindLifecycleTask) {
                IAsyncTask task = (IAsyncTask) msg.obj;
                AsyncExecutor.AsyncCallback callback = task.getCallback();
                if (callback.isStop()) {
                    callback.onCancel(true);
                    return true;
                }
                callback.onRunAfter(callback.getResult());
                if (callbackList.containsKey(msg.what)) {
                    List<BindLifecycleTask> list = callbackList.get(msg.what);
                    list.remove(task);
                    if (list.isEmpty()) {
                        callbackList.remove(msg.what);
                    }
                }
            }
        }
        return true;
    }
}