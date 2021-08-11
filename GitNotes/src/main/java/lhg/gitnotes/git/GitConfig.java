package lhg.gitnotes.git;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import lhg.gitnotes.git.ui.TransportConfigCallbackImpl;
import lhg.gitnotes.git.ui.UsernamePasswordCredentialsProviderImpl;
import lhg.common.utils.Encrypt;
import lhg.common.utils.FileUtils;
import lhg.common.utils.RegexUtil;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

@Keep
public class GitConfig implements Serializable {
    public static final String UserName = "GitNotes";
    public static final String UserEmail = "GitNotes@git.com";
    public static final String ConfigFileName = ".note_config";
    public static final String FolderLockFile = ".lock";

    //save in file
    private String url;
    private String username;
    private String password;

    //only memery
    private String repoName;
    private String rootDir;
    private String repoDir;

    static final String encode = "utf-8";
    public static GitConfig load(Context context, File path) {
        File file = new File(path, ConfigFileName);
        try {
            String json = FileUtils.readFile(file, encode);
            GitConfig obj = new Gson().fromJson(json, GitConfig.class);
            obj.initUrl(context, obj.url);
            return obj;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void initUrl(Context context, String url) {
        this.url = url;
        this.rootDir = GitConfig.genNoteRoot(context, url);
        this.repoDir = GitConfig.getRepoDir(rootDir);
        this.repoName = GitConfig.getRepoNameFromUrl(url);
    }

    public void save() {
        try {
            String repoPath = rootDir + "/" + ConfigFileName;
            String json = new Gson().toJson(this);
            FileUtils.write(repoPath, json.getBytes(encode));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEquals(GitConfig o1, GitConfig o2) {
        if (o1 == null || o2 == null) {
            return o1 == o2;
        }
        return TextUtils.equals(o1.url, o2.url);
    }

    public String getName() {
        return TextUtils.isEmpty(username) ? UserName : username;
    }
    public String getEmail() {
        if (RegexUtil.isEmail(username)) {
            return username;
        }
        return UserEmail;
    }

    public String getRelativePath(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith(repoDir + File.separator)) {
            return null;
        }
        return FileUtils.removeLastSeparator(path.substring(repoDir.length() + 1));
    }
    ////////////////////////////

    public String getPassword() {
        return password;
    }

    public String getRepoDir() {
        return repoDir;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getUrl() {
        return url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPublicKeyFile() {
        return rootDir + "/id_rsa.pub";
    }
    public String getPrivateKeyFile() {
        return rootDir + "/id_rsa";
    }

    public boolean saveIfChanged(UsernamePasswordCredentialsProviderImpl provider, TransportConfigCallbackImpl callback) {
        boolean saveGitConfig = false;
        if (!TextUtils.equals(getUsername(), provider.getUsername()) || !TextUtils.equals(getPassword(), provider.getPassword())) {
            setUsername(provider.getUsername());
            setPassword(provider.getPassword());
            saveGitConfig = true;
            save();
        }
        if (!TextUtils.isEmpty(callback.getPrivateKeyPath()) && !TextUtils.isEmpty(callback.getPublicKeyPath())) {
            try {
                FileUtils.copy(callback.getPrivateKeyPath(), getPrivateKeyFile());
                FileUtils.copy(callback.getPublicKeyPath(), getPublicKeyFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return saveGitConfig;
    }

    public String getFolderLockFile(String path) {
        if (!path.startsWith(repoDir + File.separator)) {
            return null;
        }
        File file = new File(path);
        while (file != null && !FileUtils.removeLastSeparator(file.getParent()).equals(repoDir)) {
            file = file.getParentFile();
        }
        if (file != null) {
            return FileUtils.removeLastSeparator(file.getAbsolutePath()) + File.separator + FolderLockFile;
        }
        return null;
    }

    public File isRootEncryptFolder(String path) {
        return isRootEncryptFolder(new File(path));
    }
    public File isRootEncryptFolder(File file) {
        if (!file.isDirectory()) {
            return null;
        }
        File lockFile = new File(file, GitConfig.FolderLockFile);
        if (!FileUtils.removeLastSeparator(file.getParent()).equals(repoDir) || !lockFile.exists()) {
            return null;
        }
        return lockFile;
    }

    /////////////////////////////
    public static String genNoteRoot(Context context, String url) {
        String root = getAllReposDir(context) + "/" + Encrypt.MD5(url).substring(0, 16);
        new File(root + "/").mkdirs();
        return root;
    }


    public static String getRepoDir(String rootDir) {
        return rootDir + "/repo";
    }

    private static String getRepoNameFromUrl(String url) {
        int lastDot = url.lastIndexOf(".");
        int lastPath = url.lastIndexOf("/");
        if (lastDot == -1) {
            lastDot = url.length();
        }
        if (lastPath == -1) {
            lastPath = -1;
        }
        return url.substring(lastPath+1, lastDot);
    }

    public static String getAllReposDir(Context context) {
        String path = context.getFilesDir().getAbsolutePath();
        return FileUtils.removeLastSeparator(path) + "/repos";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitConfig gitConfig = (GitConfig) o;
        return Objects.equals(url, gitConfig.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "GitConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", repoName='" + repoName + '\'' +
                ", rootDir='" + rootDir + '\'' +
                ", repoDir='" + repoDir + '\'' +
                '}';
    }
}
