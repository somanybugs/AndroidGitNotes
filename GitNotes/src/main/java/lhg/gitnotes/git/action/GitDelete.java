package lhg.gitnotes.git.action;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.common.utils.FileUtils;

import org.eclipse.jgit.api.RmCommand;

import java.io.File;

public class GitDelete extends GitSync {
    File[] files;

    public GitDelete(GitConfig gitConfig, File[] files) {
        super(gitConfig, App.instance().getString(R.string.delete));
        this.files = files;
    }

    @Override
    public void onRun() throws Throwable {
        RmCommand rmCommand = getGit().rm();
        for (File file : files) {
            FileUtils.delete(file);
            rmCommand.addFilepattern(getGitConfig().getRelativePath(file.getAbsolutePath()));
        }
        rmCommand.call();
        commit("Delete Files").call();
        publishFileChangedState();
        super.onRun();
    }
}
