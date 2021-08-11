package lhg.gitnotes.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import lhg.common.utils.EmptyActivityLifecycleCallbacks;
import lhg.gitnotes.git.GitContext;

import lhg.common.BaseApplication;

import java.util.concurrent.TimeUnit;

public class App extends BaseApplication {
    private static App _instance;
    private Paramters paramters;
    private final GitContext gitContext = new GitContext();

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
//        GitEnv.name = "GitNodes";
//        GitEnv.email = "gitnodes@163.com";
        paramters = Paramters.instance(this);
        if (!paramters.HasInitApp.get(false)) {
            paramters.HasInitApp.set(true);
        }

        registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//                if (activity instanceof LocalLoginActivity) {
//                    closeAllActivityBut(activity);
//                }
            }
        });
        gitContext.init(this);
        new Thread(() -> {
            gitContext.loadAllRepos();
            gitContext.loadCurrent();
        }).start();

    }

    public GitContext getGitContext() {
        return gitContext;
    }

    public static App instance() {
        return _instance;
    }

    public static Paramters paramters() {
        return instance().paramters;
    }


    long gotoBackgroundTime = 0;
    @Override
    protected void onGotoBackground(Activity activity) {
        super.onGotoBackground(activity);
        gotoBackgroundTime = System.currentTimeMillis();
//        closeAllAfterGotoBackground();//延时20秒 关闭所有页面
    }

    @Override
    protected void onGotoForeground(Activity activity) {
        super.onGotoForeground(activity);
        closeAllBeforeGotoForcefround(activity);
    }

    private void closeAllBeforeGotoForcefround(Activity activity) {
//        if (System.currentTimeMillis() - gotoBackgroundTime > DateUtils.SECOND_IN_MILLIS * AppConstant.CloseAllActivity_After_GotoBackground_Seconds) {
//            if (activity instanceof LocalLoginActivity || activity instanceof InitLoginPasswordActivity) {
//                closeAllActivityBut(activity);
//            } else {
//                closeAllActivity();
//                activity.startActivity(new Intent(activity, LocalLoginActivity.class));
//            }
//            Utils.clearClipboard(getApplicationContext());
//        }
    }

    private void closeAllAfterGotoBackground() {
        Constraints.Builder builder = new Constraints.Builder();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CloseAllWorker.class)
                .setConstraints(builder.build())
                .setInitialDelay(AppConstant.CloseAllActivity_After_GotoBackground_Seconds, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance().enqueueUniqueWork(
                "close_all_activity", ExistingWorkPolicy.REPLACE, request
        );
    }



    public static class CloseAllWorker extends Worker {

        public CloseAllWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
            super(appContext, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            App app = (App) getApplicationContext();
            Log.i("CloseAllWorker", "doWord");
            if (!app.isFoceground) {
                app.closeAllActivity();
            }
            return Result.success();
        }
    }

}
