package com.x930073498.component.starter.runnable;

import android.os.Process;

import com.x930073498.component.starter.dispatcher.AppStartTaskDispatcher;
import com.x930073498.component.starter.task.AppStartTask;


public class AppStartTaskRunnable implements Runnable {
    private AppStartTask appStartTask;
    private AppStartTaskDispatcher appStartTaskDispatcher;

    public AppStartTaskRunnable(AppStartTask appStartTask, AppStartTaskDispatcher appStartTaskDispatcher) {
        this.appStartTask = appStartTask;
        this.appStartTaskDispatcher = appStartTaskDispatcher;
    }

    @Override
    public void run() {
        Process.setThreadPriority(appStartTask.priority());
        appStartTask.waitToNotify();
        appStartTask.run();
        appStartTaskDispatcher.satNotifyChildren(appStartTask);
        appStartTaskDispatcher.markAppStartTaskFinish(appStartTask);
    }
}
