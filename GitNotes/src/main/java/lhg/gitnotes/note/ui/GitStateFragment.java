package lhg.gitnotes.note.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;

import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitContext;
import lhg.gitnotes.git.GitService;

import org.jetbrains.annotations.NotNull;

public class GitStateFragment extends AppBaseFragment {

    TextView tvState;
    LiveData<GitService.GitState> state;
    final Runnable hideRunnable = () -> hideSelf();

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_git_state, null);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvState = findViewById(R.id.tvState);
        GitContext.instance().getGitConfig().observe(getViewLifecycleOwner(), gitConfig -> {
            if (state != null) {
                state.removeObservers(getViewLifecycleOwner());
            }
            state = GitService.instance().getGitState(gitConfig.getUrl());
            state.observe(getViewLifecycleOwner(), state -> updateState(state));
            GitService.GitState now = state.getValue();
            if (now != null && !now.isFinished()) {
                updateState(now);
            }
        });

        tvState.setOnClickListener(v -> {
            GitService.GitState now = state.getValue();
            if (now != null && now.isError()) {
                showAlert(getString(R.string.error), now.throwable.getLocalizedMessage());
                uiHandler.removeCallbacks(hideRunnable);
                uiHandler.postDelayed(hideRunnable, 3000);
            }
        });
    }

    private void updateState(GitService.GitState state) {
        showSelf();
        if (!TextUtils.isEmpty(state.message)) {
            tvState.setText(state.message);
        } else {
            tvState.setText(R.string.sync);
        }
        if (state.isBegin()) {
            tvState.setBackgroundColor(Color.DKGRAY);
        } else if (state.isProgress()) {
            tvState.setBackgroundColor(Color.DKGRAY);
        }  else if (state.isSuccess()) {
            tvState.setBackgroundColor(getResources().getColor(R.color.holo_green_light));
            uiHandler.removeCallbacks(hideRunnable);
            uiHandler.postDelayed(hideRunnable, 3000);
        } else if (state.isError()) {
            tvState.setBackgroundColor(getResources().getColor(R.color.holo_red_light));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        uiHandler.removeCallbacks(hideRunnable);
    }

    private void showSelf() {
        if (!isHidden()) {
            return;
        }
        FragmentManager fm = getParentFragmentManager();
        if (fm == null) {
            return;
        }
        fm.beginTransaction().show(this).commitAllowingStateLoss();
    }

    private void hideSelf() {
        if (isHidden()) {
            return;
        }
        FragmentManager fm = getParentFragmentManager();
        if (fm == null) {
            return;
        }
        fm.beginTransaction().hide(this).commitAllowingStateLoss();
    }
}
