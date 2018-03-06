package oms.mmc.async.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import oms.mmc.async.AsyncExecutor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<AsyncExecutor.AsyncCallback> simpleTaskCallbacks = new ArrayList<AsyncExecutor.AsyncCallback>();
    private ArrayList<AsyncExecutor.AsyncCallback> bindLifecycleCallbacks = new ArrayList<AsyncExecutor.AsyncCallback>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mSimpleTaskBtn = findViewById(R.id.simpleTaskBtn);
        Button cancelSimpleTaskBtn = findViewById(R.id.cancelSimpleTaskBtn);
        Button mBindLifecycleBtn = findViewById(R.id.bindLifecycleTaskBtn);
        Button cancelLifecycleTaskBtn = findViewById(R.id.cancelLifecycleTaskBtn);

        mSimpleTaskBtn.setOnClickListener(this);
        cancelSimpleTaskBtn.setOnClickListener(this);
        mBindLifecycleBtn.setOnClickListener(this);
        cancelLifecycleTaskBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bindLifecycleTaskBtn:
                for (int i = 0; i < 30; i++) {
                    startBindTask();
                }
                break;
            case R.id.simpleTaskBtn:
                for (int i = 0; i < 30; i++) {
                    startSimpleTask();
                }
                break;
            case R.id.cancelSimpleTaskBtn:
                for (AsyncExecutor.AsyncCallback callback : simpleTaskCallbacks) {
                    AsyncExecutor.getInstance().cancel(callback);
                }
                simpleTaskCallbacks.clear();
                break;
            case R.id.cancelLifecycleTaskBtn:
                for (AsyncExecutor.AsyncCallback callback : bindLifecycleCallbacks) {
                    AsyncExecutor.getInstance().cancel(this, callback);
                }
                bindLifecycleCallbacks.clear();
                break;
            default:
                break;
        }
    }

    private void startSimpleTask() {
        AsyncExecutor.AsyncCallback callback = new AsyncExecutor.AsyncCallback<String, Void>() {
            @Override
            public void onRunBefore() {
                super.onRunBefore();
                Log.d(TAG, "startSimpleTask onRunBefore...");
            }

            @Override
            public String onRunning() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isStop()) {
                    return null;
                }
                Log.d(TAG, "startSimpleTask onRunning...");
                return "1024";
            }

            @Override
            public void onRunAfter(String result) {
                super.onRunAfter(result);
                Log.d(TAG, "startSimpleTask onRunAfter..." + result);
            }

            @Override
            public void onCancel(boolean isLifecycleStop) {
                super.onCancel(isLifecycleStop);
                Log.d(TAG, "SimpleTask ::: 我被手动取消啦");
            }
        };
        simpleTaskCallbacks.add(callback);
        AsyncExecutor.getInstance().execute(callback);
    }

    private void startBindTask() {
        AsyncExecutor.AsyncCallback callback = new AsyncExecutor.AsyncCallback<String, Integer>() {
            @Override
            public void onRunBefore() {
                super.onRunBefore();
                Log.d(TAG, "startBindTask onRunBefore...");
            }

            @Override
            public String onRunning() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isStop()) {
                    return null;
                }
                Log.d(TAG, "startBindTask onRunning...");
                for (int i = 0; i < 7; i++) {
                    pushProgress(i);
                }
                return "10086";
            }

            @Override
            public void onRunAfter(String result) {
                super.onRunAfter(result);
                Log.d(TAG, "startBindTask onRunAfter..." + result);
            }

            @Override
            public void onCancel(boolean isLifecycleStop) {
                super.onCancel(isLifecycleStop);
                Log.d(TAG, "BindTask ::: 我被手动取消啦");
            }

            @Override
            public void onProgressUpdate(Integer progress) {
                super.onProgressUpdate(progress);
                Log.d(TAG, "onProgressUpdate --->" + progress);
            }
        };
        bindLifecycleCallbacks.add(callback);
        AsyncExecutor.getInstance().execute(this, callback);
    }
}