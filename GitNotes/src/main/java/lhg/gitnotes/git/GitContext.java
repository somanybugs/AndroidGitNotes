package lhg.gitnotes.git;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import lhg.gitnotes.app.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitContext {

    private App app;
    private final MutableLiveData<GitConfig> gitConfig = new MutableLiveData<>();
    private final MutableLiveData<List<GitConfig>> allGitConfigs = new MutableLiveData<>();

    public void init(App app) {
        this.app = app;
        gitConfig.observeForever(gitConfig -> {
            App.paramters().LastOpenGitRoot.set(gitConfig == null ? null : gitConfig.getRootDir());
        });
    }

    public static GitContext instance() {
        return App.instance().getGitContext();
    }

    public List<GitConfig> loadAllRepos() {
        List<GitConfig> list = new ArrayList<>();
        File[] files = new File(GitConfig.getAllReposDir(app)).listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isFile()) {
                    continue;
                }
                GitConfig gc = GitConfig.load(app, f);
                if (gc != null) {
                    list.add(gc);
                }
            }
        }
        allGitConfigs.postValue(list);
        return list;
    }

    public MutableLiveData<List<GitConfig>> getAllGitConfigs() {
        return allGitConfigs;
    }

    public MutableLiveData<GitConfig> getGitConfig() {
        return gitConfig;
    }

    public void loadCurrent() {
        String rootDir = App.paramters().LastOpenGitRoot.get();
        GitConfig gitConfig = null;
        if (!TextUtils.isEmpty(rootDir)) {
            gitConfig = GitConfig.load(app, new File(rootDir));
        }
        if (gitConfig == null ) {
            List<GitConfig> gitConfigs = allGitConfigs.getValue();
            if (gitConfigs != null && !gitConfigs.isEmpty()) {
                gitConfig = gitConfigs.get(0);
            }
        }
        this.gitConfig.postValue(gitConfig);
    }

    public void updateGitConfig(GitConfig gitConfig) {
        this.gitConfig.postValue(gitConfig);
        loadAllRepos();
    }
}
