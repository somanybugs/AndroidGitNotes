package lhg.gitnotes.git.action;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.utils.AESUtls;
import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.FileUtils;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.RmCommand;

import java.io.File;

public class GitMove extends GitSync {
    File[] srcFiles;
    byte[] srcPassword;
    File destFolder;
    byte[] destPassword;

    public GitMove(GitConfig gitConfig, File[] srcFiles, byte[] srcPassword, File destFolder, byte[] destPassword) {
        super(gitConfig, App.instance().getString(R.string.move));
        this.srcFiles = srcFiles;
        this.srcPassword = srcPassword;
        this.destFolder = destFolder;
        this.destPassword = destPassword;
    }

    private void checkMove() {
        String destPath = FileUtils.removeLastSeparator(destFolder.getAbsolutePath()) + File.separator;
        for (File src : srcFiles) {
            if (src.getName().equals(".") || src.getName().equals("..")) {
                throw new IllegalStateException(getGitConfig().getRelativePath(src.getAbsolutePath()) + " can`t be moved");
            }

            if (getGitConfig().isRootEncryptFolder(src) != null) {
                throw new IllegalStateException(App.instance().getString(R.string.forbid_move_encrypt_folder) + " " + getGitConfig().getRelativePath(src.getAbsolutePath()));
            }

            if (src.isFile()) {
                continue;
            }

            String srcPath = FileUtils.removeLastSeparator(src.getAbsolutePath()) + File.separator;
            if (destPath.startsWith(srcPath)) {// dest is child of src, cant move
                throw new IllegalStateException(getGitConfig().getRelativePath(srcPath) + " can`t be moved to " + getGitConfig().getRelativePath(destPath));
            }
        }
    }

    @Override
    public void onRun() throws Throwable {
        checkMove();
        RmCommand rmCommand = getGit().rm();
        AddCommand addCommand = getGit().add();

        for (File srcFile : srcFiles) {
            File destFile = new File(destFolder, srcFile.getName());
            rename(srcFile, destFile);
            rmCommand.addFilepattern(getGitConfig().getRelativePath(srcFile.getAbsolutePath()));
            addCommand.addFilepattern(getGitConfig().getRelativePath(destFile.getAbsolutePath()));
            FileUtils.delete(srcFile);
        }
        rmCommand.call();
        addCommand.call();
        commit("Move Files to " + getGitConfig().getRelativePath(destFolder.getAbsolutePath())).call();
        publishFileChangedState();
        super.onRun();
    }

    private File getTmpFile(String suffix) {
        File file = new File(App.instance().getCacheDir(), "git_folder_encrypt.tmp." + suffix);
        file.delete();
        return file;
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
            File tmpFile1;
            boolean supportEncrypt = AppConstant.FileSuffix.support(srcFile.getName());
            if (srcPassword != null && supportEncrypt) {
                tmpFile1 = getTmpFile("1");
                AESUtls.decryptFile2File(srcPassword, srcFile, tmpFile1);
            } else {
                tmpFile1 = srcFile;
            }
            File tmpFile2;
            if (destPassword != null && supportEncrypt) {
                tmpFile2 = getTmpFile("2");
                AESUtls.encryptFile2File(destPassword, tmpFile1, tmpFile2);
            } else {
                tmpFile2 = tmpFile1;
            }

            destFile.delete();
            tmpFile2.renameTo(destFile);
        }

    }
}
