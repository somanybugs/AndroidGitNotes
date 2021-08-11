package lhg.gitnotes.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.ActionBar;

import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.app.AppBaseActivity;
import lhg.gitnotes.utils.AESUtls;
import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.EncodingDetect;
import lhg.common.utils.FileUtils;
import lhg.common.utils.Utils;

import java.io.File;

public class FileViewer extends AppBaseActivity {
    public static final String IntentKey_Path = "path";
    public static final String IntentKey_GitConfig = "gitConfig";
    public static final String IntentKey_Password = "password";
    protected static final int RequestCode_Edit = 111;

    private static final String TAG = "FileViewer";
    protected File parent, file;
    protected String password;
    protected byte[] key;
    protected GitConfig gitConfig;

    protected static Intent makeIntent(Context context, Class<? extends FileViewer> clazz, String path, GitConfig gitConfig, String password) {
        Intent intent = new Intent(context, clazz);
        intent.putExtra(IntentKey_Path, path);
        intent.putExtra(IntentKey_GitConfig, gitConfig);
        intent.putExtra(IntentKey_Password, password);
        return intent;
    }

    protected void setTitleAndSubtitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(file.getName());
            String subtitle = FileUtils.removeLastSeparator(gitConfig.getRelativePath(file.getParent()));
            if (!TextUtils.isEmpty(subtitle)) {
                actionBar.setSubtitle(subtitle);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initOnCreate()) {
            finish();
            return;
        }
    }

    protected byte[] readFileBuffer() throws Exception {
        if (password != null && AppConstant.FileSuffix.support(file.getName())) {
            return AESUtls.decryptFile2bytes(key, file);
        } else {
            return FileUtils.readFileBuff(file);
        }
    }

    protected String readFileText() throws Exception {
        byte[] buffer = readFileBuffer();
        if (buffer == null) {
            return null;
        }
        String encode = EncodingDetect.detect(buffer);
        return new String(buffer, encode);
    }


    protected boolean initOnCreate() {
        if (getIntent() == null) {
            return false;
        }
        String path = getIntent().getStringExtra(IntentKey_Path);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        file = new File(path);
        if (file.isDirectory()) {
            finish();
            return false;
        }
        password = getIntent().getStringExtra(IntentKey_Password);
        if (!TextUtils.isEmpty(password)) {
            key = Utils.hexToBytes(password);
        }
        gitConfig = (GitConfig) getIntent().getSerializableExtra(IntentKey_GitConfig);
        parent = file.getParentFile();
        return true;
    }

}
