package lhg.gitnotes.git.action;

import android.text.TextUtils;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;

import org.eclipse.jgit.api.AddCommand;

import java.io.File;

public class GitAdd extends GitSync {
    File[] files;
    String message;
    public GitAdd(GitConfig gitConfig, File... files)  {
        super(gitConfig, App.instance().getString(R.string.modify));
        this.files = files;
    }

    public GitAdd setMessage(String message) {
        this.message = message;
        if (!TextUtils.isEmpty(message)) {
            setName(message);
        }
        return this;
    }

    @Override
    public void onRun() throws Throwable {
        AddCommand addCommand = getGit().add();
        for (File f : files) {
            addCommand.addFilepattern(getGitConfig().getRelativePath(f.getAbsolutePath()));
        }
        addCommand.call();
        commit(TextUtils.isEmpty(message) ? "Modify Files" : message).call();
        publishFileChangedState();
        super.onRun();
    }
}
