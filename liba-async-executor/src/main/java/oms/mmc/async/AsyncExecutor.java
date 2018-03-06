package oms.mmc.async;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import oms.mmc.async.manager.BindLifecycleTaskManager;
import oms.mmc.async.manager.ITaskManager;
import oms.mmc.async.manager.SimpleTaskManager;
import oms.mmc.async.task.BindLifecycleTask;
import oms.mmc.async.task.IAsyncTask;
import oms.mmc.async.task.SimpleTask;

/**
 * Package: oms.mmc.async
 * FileName: AsyncExecutor
 * Date: on 2018/3/4  下午4:50
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class AsyncExecutor {
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 普通任务管理器
     */
    private final SimpleTaskManager mSimpleTaskManager;
    /**
     * 绑定生命周期任务管理器
     */
    private final BindLifecycleTaskManager mLifecycleTaskManager;
    /**
     * 主线程Handler
     */
    private final Handler mMainHandler;

    private static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE = 10;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new SynchronousQueue<Runnable>();
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "AsyncExecutor #" + mCount.getAndIncrement());
        }
    };

    private static final class SingleHolder {
        private static final AsyncExecutor instance = new AsyncExecutor();
    }

    public AsyncExecutor() {
        //线程池
        mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
        //主线程Hanlder
        mMainHandler = new Handler(Looper.getMainLooper());
        mSimpleTaskManager = new SimpleTaskManager(mThreadPool);
        mLifecycleTaskManager = new BindLifecycleTaskManager(mThreadPool);
    }

    public static AsyncExecutor getInstance() {
        return SingleHolder.instance;
    }

    public void execute(final AsyncCallback<?, ?> callback) {
        execute(null, callback);
    }

    public void execute(final Activity activity, final AsyncCallback<?, ?> callback) {
        if (callback == null) {
            return;
        }
        int symbol;
        IAsyncTask task;
        ITaskManager taskManager;
        callback.setMainHandler(mMainHandler);
        if (activity == null) {
            //不绑定Activity生命周期的任务
            symbol = callback.hashCode();
            task = new SimpleTask(symbol, callback);
            taskManager = mSimpleTaskManager;
        } else {
            //绑定Activity生命周期的任务
            symbol = activity.hashCode();
            task = new BindLifecycleTask(symbol, callback);
            taskManager = mLifecycleTaskManager;
            mLifecycleTaskManager.registerLifecycle(activity.getApplication());
        }
        taskManager.addTask(symbol, task);
        callback.onRunBefore();
        taskManager.executeTask(symbol, task);
    }

    public void cancel(AsyncCallback callback) {
        cancel(null, callback);
    }

    public void cancel(Activity activity, AsyncCallback callback) {
        if (activity == null) {
            mSimpleTaskManager.cancel(callback.hashCode(), callback);
        } else {
            mLifecycleTaskManager.cancel(activity.hashCode(), callback);
        }
    }

    public abstract static class AsyncCallback<Result, Progress> {
        /**
         * 是否已经停止了
         */
        private boolean isStop = false;
        /**
         * 结果
         */
        private Object result;
        private Handler mMainHandler;

        public void setMainHandler(Handler mainHandler) {
            this.mMainHandler = mainHandler;
        }

        /**
         * 在任务开始之前回调
         */
        public void onRunBefore() {
        }

        /**
         * 任务进行中，在子线程回调，做耗时操作
         *
         * @return 返回结果
         */
        public abstract Result onRunning();

        /**
         * 任务结束后回调
         *
         * @param result 结果
         */
        public void onRunAfter(Result result) {
        }

        /**
         * 被手动取消时回调
         *
         * @param isLifecycleStop 是否是因为生命周期停止而取消
         */
        public void onCancel(boolean isLifecycleStop) {
        }

        public final void pushProgress(final Progress progress) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressUpdate(progress);
                }
            });
        }

        public void onProgressUpdate(Progress progress) {
        }

        /**
         * 任务是否已经停止了
         */
        public boolean isStop() {
            return isStop;
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Object getResult() {
            return result;
        }
    }
}