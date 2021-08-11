package lhg.gitnotes.ui;

import android.text.TextUtils;

import lhg.gitnotes.git.GitService;
import lhg.gitnotes.git.action.GitAdd;
import lhg.gitnotes.utils.AESUtls;
import lhg.gitnotes.utils.ThreadUtils;
import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.FileUtils;

import java.io.ByteArrayInputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

public abstract class FileEditor extends FileViewer {



    protected boolean isNewFile = false;

    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }

        isNewFile = !file.exists();
        return true;
    }

    protected void writeFileBuffer(byte[] buffer) throws Exception {
        file.delete();
        if (buffer == null || buffer.length == 0) {
            file.createNewFile();
            return;
        }
        if (password != null && AppConstant.FileSuffix.support(file.getName())) {
            AESUtls.encryptIs2File(key, new ByteArrayInputStream(buffer), file);
        } else {
            FileUtils.write(file, buffer);
        }
    }

    protected void writeFileText(String text) throws Exception {
        writeFileBuffer(TextUtils.isEmpty(text) ? null : text.getBytes("utf-8"));
    }

    protected void saveLocalFile() {
        Completable.fromAction(() -> {
            onSaveLocalFile();
            setResult(RESULT_OK);
            GitService.instance().submit(new GitService.GitAction(gitConfig, null) {
                @Override
                public void onRun() throws Throwable {
                    getGit().add().addFilepattern(getGitConfig().getRelativePath(file.getAbsolutePath())).call();
                }
            });
        }).subscribeOn(ThreadUtils.FILE_SCHEDULER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    protected abstract void onSaveLocalFile() throws Throwable;

    protected void gitSync(Runnable success) {
        showLoadingDialog();
        Completable.fromAction(() -> {
            onSaveLocalFile();
            setResult(RESULT_OK);
        }).subscribeOn(ThreadUtils.FILE_SCHEDULER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    String message = (isNewFile ? "Add " : "Modify ") + file.getName();
                    GitService.instance().submit(new GitAdd(gitConfig, file).setMessage(message));
                    hideLoadingDialog();
                    if (success != null) {
                        success.run();
                    }
                }, throwable -> {
                    hideLoadingDialog();
                    throwable.printStackTrace();
                });
    }

}
