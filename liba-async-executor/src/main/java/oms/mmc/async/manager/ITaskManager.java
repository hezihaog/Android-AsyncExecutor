package oms.mmc.async.manager;

import oms.mmc.async.AsyncExecutor;
import oms.mmc.async.task.IAsyncTask;

/**
 * Package: oms.mmc.async
 * FileName: ITaskManager
 * Date: on 2018/3/4  下午8:41
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public interface ITaskManager<T extends IAsyncTask> {
    /**
     * 添加一个任务
     *
     * @param symbol 任务标识
     * @param task   任务对象
     */
    void addTask(int symbol, T task);

    /**
     * 取消
     *
     * @param symbol   任务标识
     * @param callback 使用者的回调
     */
    void cancel(int symbol, AsyncExecutor.AsyncCallback callback);

    /**
     * 执行任务
     *
     * @param symbol 任务标识
     * @param task           任务对象
     * @return 是否正确执行
     */
    boolean executeTask(int symbol, T task);
}