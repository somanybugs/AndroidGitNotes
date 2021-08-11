package lhg.gitnotes.git;

import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.common.utils.NamedThreadFactory;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.internal.schedulers.ExecutorScheduler;

public class GitService {


    private static GitService service;
    private Map<String, GitHolder> gits = new HashMap<>();

    public static GitService instance() {
        if (service == null) {
            synchronized (GitService.class) {
                if (service == null) {
                    service = new GitService();
                }
            }
        }
        return service;
    }

    private GitHolder getGitHolder(String url) {
        synchronized (gits) {
            GitHolder gitHolder = gits.get(url);
            if (gitHolder == null) {
                gitHolder = new GitHolder();
                gitHolder.scheduler = new ExecutorScheduler(Executors.newSingleThreadExecutor(new NamedThreadFactory(url)), true, true);
                long mills = 10 * DateUtils.MINUTE_IN_MILLIS;
                GitHolder finalGitHolder = gitHolder;
                gitHolder.scheduler.schedulePeriodicallyDirect(() -> {
                    GitState state = finalGitHolder.state.getValue();
                    if (state != null && state.isFinished() && state.time + mills < System.currentTimeMillis()) {
                        if (finalGitHolder.git != null) {
                            finalGitHolder.git.close();
                            finalGitHolder.git = null;
                        }
                    }
                }, mills, mills, TimeUnit.MILLISECONDS);
                gits.put(url, gitHolder);
            }
            return gitHolder;
        }
    }

    public Scheduler getScheduler(String url) {
        return getGitHolder(url).scheduler;
    }

    private Git getGit(String url) {
        return openGit(url, false);
    }

    private Git openGit(String url, boolean increateOpen) {
        synchronized (gits) {
            GitHolder gitHolder = getGitHolder(url);
            if (gitHolder.git == null) {
                try {
                    String rootDir = GitConfig.genNoteRoot(App.instance(), url);
                    gitHolder.git = Git.open(new File(GitConfig.getRepoDir(rootDir)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (increateOpen && gitHolder.git != null) {
                gitHolder.git.getRepository().incrementOpen();
            }
            return gitHolder.git;
        }
    }


    public List<GitState> getGitErrors(String url) {
        synchronized (gits) {
            GitHolder gitHolder = getGitHolder(url);
            return new ArrayList<>(gitHolder.errors);
        }
    }

    public LiveData<GitState> getGitState(String url) {
        synchronized (gits) {
            return getGitHolder(url).state;
        }
    }

    public Scheduler getScheduler(GitConfig gitConfig) {
        return getScheduler(gitConfig.getUrl());
    }

    public void submit(GitAction action) {
        GitHolder gitHolder = getGitHolder(action.gitConfig.getUrl());
        action.progressMonitor = gitHolder.progressMonitor;
        final String url = action.gitConfig.getUrl();
        final String uuid = action.uuid;
        final String tips = action.name;
        getScheduler(url).scheduleDirect(() -> {
            try {
                getGitHolder(url).postState(GitState.begin(uuid, tips));
                action.run();
                getGitHolder(url).postState(GitState.success(uuid, tips));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                getGitHolder(url).postState(GitState.error(uuid, throwable, tips));
            }
        });
    }

    public static abstract class GitAction implements Action {
        private GitConfig gitConfig;
        private String uuid = UUID.randomUUID().toString();
        private Git git;
        private ProgressMonitor progressMonitor;
        private String name;

        public GitAction(GitConfig gitConfig, String name) {
            this.gitConfig = gitConfig;
            this.name = name;
        }

        public ProgressMonitor getProgressMonitor() {
            return progressMonitor;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Git getGit() {
            return git;
        }

        public GitConfig getGitConfig() {
            return gitConfig;
        }

        public String getUuid() {
            return uuid;
        }

        @Override
        public final void run() throws Throwable {
            try {
                git = GitService.instance().openGit(gitConfig.getUrl(), true);
                onRun();
            } finally {
                if (git != null) {
                    git.getRepository().close();
                }
            }
        }

        public abstract void onRun() throws Throwable;

        protected void publishFileChangedState() {
            GitService.instance().getGitHolder(gitConfig.getUrl()).postState(new GitState(GitState.STATE_PROGRESS, uuid, null, "", true));
        }
    }

    public static class GitState {
        public final Throwable throwable;
        public final long time = System.currentTimeMillis();
        public final String uuid;
        public final String message;
        public final int state;//
        public final boolean fileChanged;
        private static final int STATE_BEGIN = 0;
        private static final int STATE_SUCCESS = 1;
        private static final int STATE_PROGRESS = 2;
        private static final int STATE_ERROR = 3;

        public GitState(int state, String uuid, Throwable throwable, String message, boolean fileChanged) {
            this.state = state;
            this.throwable = throwable;
            this.uuid = uuid;
            this.message = message;
            this.fileChanged = fileChanged;
        }

        private static String name(String name) {
            if (TextUtils.isEmpty(name)) {
                return App.instance().getString(R.string.sync);
            } else {
                return name;
            }
        }

        public static GitState begin(String uuid, String name) {
            String message = name(name) + "...";
            return new GitState(STATE_BEGIN, uuid, null, message, false);
        }

        public static GitState progress(String uuid, String message) {
            if (TextUtils.isEmpty(message) || message.trim().isEmpty()) {
                message = App.instance().getString(R.string.sync) + "...";
            }
            return new GitState(STATE_PROGRESS, uuid, null, message, false);
        }

        public static GitState success(String uuid, String name) {
            String message = name(name) + " " + App.instance().getString(R.string.success);
            ;
            return new GitState(STATE_SUCCESS, uuid, null, message, true);
        }

        public static GitState error(String uuid, Throwable throwable, String name) {
            String error = App.instance().getString(R.string.error) + ": " + throwable.getLocalizedMessage();
            String message = name(name) + " " + error;
            return new GitState(STATE_ERROR, uuid, throwable, message, true);
        }

        public boolean isSuccess() {
            return state == STATE_SUCCESS;
        }

        public boolean isError() {
            return state == STATE_ERROR;
        }

        public boolean isBegin() {
            return state == STATE_BEGIN;
        }

        public boolean isProgress() {
            return state == STATE_PROGRESS;
        }

        public boolean isFinished() {
            return state == STATE_ERROR || state == STATE_SUCCESS;
        }
    }


    private static class GitHolder {
        Scheduler scheduler;
        Git git;

        final ProgressMonitor progressMonitor = new TextProgressMonitor() {
            @Override
            protected void send(StringBuilder s) {
                GitState last = state.getValue();
                String uuid = last == null ? null : last.uuid;
                postState(GitState.progress(uuid, s.toString()));
            }
        };
        final MutableLiveData<GitState> state = new MutableLiveData<>();
        final ArrayDeque<GitState> errors = new ArrayDeque<>(100);

        void postState(GitState state) {
            this.state.postValue(state);
            if (state.isError()) {
                errors.offer(state);
            }
        }
    }

}
