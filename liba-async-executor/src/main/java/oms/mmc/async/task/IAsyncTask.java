package oms.mmc.async.task;

import oms.mmc.async.AsyncExecutor;

/**
 * Package: oms.mmc.async.task
 * FileName: IAsyncTask
 * Date: on 2018/3/4  下午6:36
 * Auther: zihe
 * Descirbe:任务接口
 * Email: hezihao@linghit.com
 */

public interface IAsyncTask {
    /**
     * 获取任务的回调对象
     *
     * @return 任务的回调对象
     */
    AsyncExecutor.AsyncCallback<?, ?> getCallback();
}