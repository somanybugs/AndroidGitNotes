package lhg.gitnotes.git.action;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.common.utils.FileUtils;

import java.io.File;

public class GitRename extends GitSync {
    File file;
    String name;



    public GitRename(GitConfig gitConfig, File file, String name) {
        super(gitConfig, App.instance().getString(R.string.rename));
        this.file = file;
        this.name = name;
    }

    @Override
    public void onRun() throws Throwable {
        if (FileUtils.isDotDotFile(file.getName()) || FileUtils.isDotDotFile(name)) {
            throw new IllegalArgumentException(file.getName() + " can`t be renamed to " + name);
        }
        File destFile = new File(file.getParent(), name);
        if (destFile.exists()) {
            throw new IllegalArgumentException(name + " already exists");
        }

        rename(file, destFile);
        FileUtils.delete(file);

        getGit().rm().addFilepattern(getGitConfig().getRelativePath(file.getAbsolutePath())).call();
        getGit().add().addFilepattern(getGitConfig().getRelativePath(destFile.getAbsolutePath())).call();
        commit("Rename " + file.getName() + " to " + name).call();

        publishFileChangedState();
        super.onRun();
    }

    private void rename(File srcFile, File destFile) throws Exception {
        if (srcFile.isDirectory()) {
            destFile.mkdirs();
            File[] files = srcFile.listFiles();
            if (files != null) {
                for (File f: files) {
                    rename(f, new File(destFile, f.getName()));
                }
            }
        } else {
            destFile.delete();
            srcFile.renameTo(destFile);
        }
    }
}
