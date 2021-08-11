package lhg.gitnotes.git.action;

import android.text.TextUtils;

import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.git.GitContext;
import lhg.gitnotes.git.GitService;
import lhg.gitnotes.git.ui.TransportConfigCallbackImpl;
import lhg.gitnotes.git.ui.UsernamePasswordCredentialsProviderImpl;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.merge.MergeStrategy;

import java.io.File;
import java.util.Set;

public class GitSync extends GitService.GitAction {

    private UsernamePasswordCredentialsProviderImpl credentialsProvider;
    private TransportConfigCallbackImpl transportConfigCallback;

    public GitSync(GitConfig gitConfig, String tips) {
        super(gitConfig, tips);
    }

    private boolean needRetryWhenTransportException() {
        return !TextUtils.isEmpty(getGitConfig().getUsername()) || !TextUtils.isEmpty(getGitConfig().getPassword()) || new File(getGitConfig().getPrivateKeyFile()).exists();
    }

    @Override
    public void onRun() throws Throwable {
        credentialsProvider = new UsernamePasswordCredentialsProviderImpl(getGitConfig().getUsername(), getGitConfig().getPassword());
        transportConfigCallback = new TransportConfigCallbackImpl(getGitConfig().getPrivateKeyFile());
        boolean hasTransportException = false;
        while (true) {
            try {
                callAndThrow();
            } catch (TransportException e) {
                e.printStackTrace();
                if (needRetryWhenTransportException() && !hasTransportException) {
                    credentialsProvider = new UsernamePasswordCredentialsProviderImpl(null, null);
                    transportConfigCallback = new TransportConfigCallbackImpl(null);
                    hasTransportException = true;
                    continue;
                }
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                Status status = getGit().status().call();
                Set<String> set = status.getConflicting();
                if (set != null && !set.isEmpty()) {
                    for (String s : set) {
                        getGit().add().addFilepattern(s).call();
                    }
                    callAndThrow();
                } else {
                    throw e;
                }
            }
            break;
        }
        if (getGitConfig().saveIfChanged(credentialsProvider, transportConfigCallback)) {
            GitContext.instance().updateGitConfig(getGitConfig());
        }
    }

    protected CommitCommand commit(String message) {
        return getGit().commit().setAuthor(getGitConfig().getName(), getGitConfig().getEmail()).setMessage(message);
    }

    private void callAndThrow() throws Exception {
        if (!getGit().diff().setCached(true).call().isEmpty()) {
            commit("missing commit").call();
        }

        PullCommand pullCommand = getGit().pull();
        pullCommand.setStrategy(MergeStrategy.OURS);
        pullCommand.setCredentialsProvider(credentialsProvider);
        pullCommand.setTransportConfigCallback(transportConfigCallback);
        pullCommand.setProgressMonitor(getProgressMonitor());
        pullCommand.call();

        publishFileChangedState();

        if (!getGit().diff().setCached(true).call().isEmpty()) {
            commit("merge").call();
        }

        PushCommand pushCommand = getGit().push();
        pushCommand.setCredentialsProvider(credentialsProvider);
        pushCommand.setTransportConfigCallback(transportConfigCallback);
        pushCommand.setProgressMonitor(getProgressMonitor());
        pushCommand.call();
    }
}
