package lhg.gitnotes.app;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import lhg.common.activity.BaseActivity;
import lhg.common.view.LoadingDialog;

/**
 * Author: liuhaoge
 * Date: 2020/11/26 20:19
 * Note:
 */
public class AppBaseActivity extends BaseActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(getClass().getName(), "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.i(getClass().getName(), "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(getClass().getName(), "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.i(getClass().getName(), "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.i(getClass().getName(), "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.i(getClass().getName(), "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(getClass().getName(), "onDestroy");
    }


}
