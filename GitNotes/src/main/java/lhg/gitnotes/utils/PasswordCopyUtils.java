package lhg.gitnotes.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * Author: liuhaoge
 * Date: 2020/11/29 19:05
 * Note:
 */
public class PasswordCopyUtils {

    public static void copy(Context context, String value) {
        Utils.copy2Clipboard(context, value);
        int seconds = AppConstant.Password_Clip_AutoClear_Seconds;
        clearClipAfterSomeTime(seconds);
        ToastUtil.showLong(context, "已复制," + seconds + "秒后会自动从剪切板清除内容");
    }

    private static void clearClipAfterSomeTime(int seconds) {
        Constraints.Builder builder = new Constraints.Builder();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ClearAllWorker.class)
                .setConstraints(builder.build())
                .setInitialDelay(seconds, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance().enqueueUniqueWork(
                "clear_clip_password", ExistingWorkPolicy.REPLACE, request
        );
    }


    public static class ClearAllWorker extends Worker {

        public ClearAllWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
            super(appContext, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Log.i("ViewPasswordDialog", "已清除复制数据");
            Utils.clearClipboard(getApplicationContext());
            return Result.success();
        }
    }
}
