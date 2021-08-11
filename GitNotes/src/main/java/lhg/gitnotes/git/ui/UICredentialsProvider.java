package lhg.gitnotes.git.ui;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;

import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;


public class UICredentialsProvider {

    Handler handler = new Handler(Looper.getMainLooper());
    ConditionVariable inputBlock = new ConditionVariable();
    FragmentManager fm;
    String result;

    public UICredentialsProvider(FragmentManager fm) {
        this.fm = fm;
    }

    public String call(String param) {
        handler.post(() -> {
            if ("userpass".equals(param)) {
                new GitRepoInputUserpassDialogFragment(new DialogInputCallback() {
                    @Override
                    public void onInput(String... vals) {
                        result = new Gson().toJson(new Userpass(vals[0], vals[1]));
                        inputBlock.open();
                    }

                    @Override
                    public void onDismiss() {
                        inputBlock.open();
                    }
                }).show(fm, "GitRepoInputUserpassDialogFragment");
            } else if ("ssh".equals(param)) {
                new GitRepoInputSshDialogFragment(new DialogInputCallback() {
                    @Override
                    public void onInput(String... vals) {
                        result = new Gson().toJson(new SSHKeyPath(vals[0], vals[1]));
                        inputBlock.open();
                    }

                    @Override
                    public void onDismiss() {
                        inputBlock.open();
                    }
                }).show(fm, "GitRepoInputSshDialogFragment");
            } else {
                inputBlock.open();
            }
        });
        inputBlock.block(DateUtils.MINUTE_IN_MILLIS * 30);
        return result;
    }

    public static class SSHKeyPath {
        public String public_key_path;
        public String private_key_path;

        public SSHKeyPath(String public_key_path, String private_key_path) {
            this.public_key_path = public_key_path;
            this.private_key_path = private_key_path;
        }
    }
    public static class Userpass {
        public String username;
        public String password;

        public Userpass(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
