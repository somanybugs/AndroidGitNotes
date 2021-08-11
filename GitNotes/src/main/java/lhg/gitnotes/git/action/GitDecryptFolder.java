package lhg.gitnotes.git.action;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.utils.AESUtls;
import lhg.gitnotes.utils.EncryptFolderUtils;
import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.FileUtils;

import java.io.File;

public class GitDecryptFolder extends GitSync {

    final File folder;
    final String password;

    public GitDecryptFolder(GitConfig gitConfig, String folder, String password) {
        super(gitConfig, App.instance().getString(R.string.decrypt_folder));
        this.folder = new File(folder);
        this.password = password;
    }

    @Override
    public void onRun() throws Throwable {
        if (!FileUtils.removeLastSeparator(folder.getParent()).equals(getGitConfig().getRepoDir())) {
            throw new IllegalArgumentException("Only sub folders of root could be decrypt!");
        }
        File lockFile = new File(folder, GitConfig.FolderLockFile);
        byte[] key = EncryptFolderUtils.decryptKey(password, lockFile);
        encryptFolder(key, folder);
        getGit().rm().addFilepattern(getGitConfig().getRelativePath(lockFile.getAbsolutePath())).call();
        getGit().add().addFilepattern(getGitConfig().getRelativePath(folder.getAbsolutePath())).call();
        commit("Decrypt Folder " + folder.getName()).call();
        publishFileChangedState();
        super.onRun();
    }

    private void encryptFolder(byte[] key, File folder) throws Throwable {
        File files[] = folder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File f : files) {
            if (f.getName().equals(".") || f.getName().equals("..")) {
                continue;
            }
            if (f.isDirectory()) {
                encryptFolder(key, f);
            } else {
                if (!AppConstant.FileSuffix.support(f.getName())) {
                    continue;
                }
                File tmpFile = new File(App.instance().getCacheDir(), "git_folder_decrypt.tmp");
                tmpFile.delete();
                try {
                    AESUtls.decryptFile2File(key, f, tmpFile);
                    tmpFile.renameTo(f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
