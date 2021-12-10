package lhg.gitnotes.ui;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import lhg.common.utils.FileUtils;
import lhg.gitnotes.app.AppConstant;
import lhg.gitnotes.git.GitService;
import lhg.gitnotes.git.action.GitAdd;
import lhg.gitnotes.utils.AESUtls;
import lhg.gitnotes.utils.ThreadUtils;

public abstract class FileEditor<T> extends FileViewer<T> {

    protected boolean isNewFile = false;
    private final SaveState saveState = new SaveState();
    private Disposable saveFileDisposable;
    private final Runnable saveFileTask = () -> {
        trySaveFile();
        GitService.instance().submit(new GitService.GitAction(gitConfig, null) {
            @Override
            public void onRun() throws Throwable {
                getGit().add().addFilepattern(getGitConfig().getRelativePath(file.getAbsolutePath())).call();
            }
        });
    };

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
        saveState.request();
        if (saveFileDisposable != null) {
            saveFileDisposable.dispose();
        }
        saveFileDisposable = ThreadUtils.FILE_SCHEDULER.scheduleDirect(saveFileTask, 5, TimeUnit.SECONDS);
    }

    protected void gitSync() {
        String message = (isNewFile ? "Add " : "Modify ") + file.getName();
        GitService.instance().submit(new GitAdd(gitConfig, file) {
            @Override
            public void onRun() throws Throwable {
                if (saveState.needSave()) {
                    trySaveFile();
                }
                getGit().add().addFilepattern(getGitConfig().getRelativePath(file.getAbsolutePath())).call();
                super.onRun();
            }
        }.setMessage(message));
    }

    protected void setContentChanged(boolean changed) {
        if (changed) {
            saveState.request();
            setResult(RESULT_OK);
        } else {
            saveState.save();
        }
    }

    private void trySaveFile() {
        try {
            ((WriteCallback)callback).onWrite();
            saveState.save();
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        if (saveState.needSave()) {
            if (saveFileDisposable != null) {
                saveFileDisposable.dispose();
            }
            trySaveFile();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (saveState.needSync()) {
            gitSync();
        }
        super.onStop();
    }

    protected abstract WriteCallback<T> onCreateCallback();

    protected interface WriteCallback<A> extends ReadCallback<A> {
        void onWrite() throws Exception;
    }

    private static class SaveState {
        long requestTime = 0;
        long savedTime = 0;
        boolean needSync = false;

        synchronized void request() {
            requestTime = System.currentTimeMillis();
            needSync = true;
        }

        synchronized void save() {
            savedTime = System.currentTimeMillis();
        }

        synchronized boolean needSave() {
            return savedTime < requestTime;
        }

        synchronized boolean needSync() {
            return needSync;
        }
    }
}
