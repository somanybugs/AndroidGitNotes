package lhg.gitnotes.git.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;

public class GitRepoAddSSHWaysFragment extends AppBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gitrepo_input_ssh_ways, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewById(R.id.btnGenNewKeys).setOnClickListener(v -> findCallback(GitRepoInputSshCallback.class).gotoGenerateNewKeys());
        findViewById(R.id.btnProvideKeys).setOnClickListener(v -> findCallback(GitRepoInputSshCallback.class).gotoProvideCustomKeys());
    }
}
