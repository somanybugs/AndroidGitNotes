package lhg.gitnotes.git.ui;

import android.content.Intent;
import android.os.Bundle;

import lhg.gitnotes.app.App;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.HomeActivity;
import lhg.common.activity.BaseActivity;
import lhg.common.utils.FileUtils;
import lhg.common.utils.FragmentHelper;
import lhg.common.utils.ToastUtil;

import org.eclipse.jgit.api.Git;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GitRepoAddActivity extends BaseActivity implements GitRepoInputUrlFragment.OnInputUrlCallback {
    Disposable disposable;
    UsernamePasswordCredentialsProviderImpl credentialsProvider;
    TransportConfigCallbackImpl transportConfigCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pcm_activity_fragment_container);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new GitRepoInputUrlFragment()).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (!FragmentHelper.onBackPressed(getSupportFragmentManager())) {
            super.onBackPressed();
        }
    }

    @Override
    public void onInputUrl(String url) {
        GitConfig gitConfig = GitConfig.load(getActivity(), new File(GitConfig.genNoteRoot(getActivity(), url)));
        if (gitConfig != null) {
            showAlert(getResources().getString(R.string.error), url + " already exits");
            return;
        }
        gitConfig = new GitConfig();
        gitConfig.initUrl(this, url);
        showProgressDialog("clone...");

        //TODO libgit2 实在垃圾 userpass模式 credentials_cb 返回-1 就崩溃
//        disposable = Completable.fromAction(() -> {
//            GitNative.init();
//            GitNative.clone(url, localPath, new CredentialsProvider(getSupportFragmentManager(), url));
//            GitNative.shutdown();
//        }).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(() -> {
//                    App.instance().getGitContext().gitConfig = gitConfig;
//                    gitConfig.save(getActivity());
//                    ToastUtil.show(getActivity(), "Clone success!!");
//                    hideProgressDialog();
//                    startActivity(new Intent(getActivity(), HomeActivity.class));
//                    getActivity().finish();
//                }, throwable -> {
//                    throwable.printStackTrace();
//                    hideProgressDialog();
//                    ToastUtil.show(getActivity(), "Clone error: " + throwable.getLocalizedMessage());
//                })
//        ;

        GitConfig finalGitConfig = gitConfig;
        disposable = Completable.fromAction(() -> {
            FileUtils.deleteDir(new File(finalGitConfig.getRootDir()));
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(new File(finalGitConfig.getRepoDir()))
                    .setCredentialsProvider(credentialsProvider = new UsernamePasswordCredentialsProviderImpl(null, null))
                    .setTransportConfigCallback(transportConfigCallback = new TransportConfigCallbackImpl(null))
                    .call()
                    .close();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {

                    if (!finalGitConfig.saveIfChanged(credentialsProvider, transportConfigCallback)) {
                        finalGitConfig.save();
                    }
                    App.instance().getGitContext().updateGitConfig(finalGitConfig);
                    ToastUtil.show(getActivity(), "Clone success!!");
                    hideProgressDialog();
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();
                }, throwable -> {
                    throwable.printStackTrace();
                    hideProgressDialog();
                    ToastUtil.show(getActivity(), "Clone error: " + throwable.getLocalizedMessage());
                })
        ;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

}
