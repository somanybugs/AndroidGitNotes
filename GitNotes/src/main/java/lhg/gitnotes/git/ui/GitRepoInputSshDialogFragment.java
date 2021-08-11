package lhg.gitnotes.git.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import lhg.gitnotes.R;
import lhg.common.OnBackPressedCallback;
import lhg.common.utils.FragmentHelper;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GitRepoInputSshDialogFragment extends AppCompatDialogFragment implements GitRepoInputSshCallback, OnBackPressedCallback {
    DialogInputCallback callback;

    public GitRepoInputSshDialogFragment(DialogInputCallback callback) {
        this.callback = callback;
        setStyle(STYLE_NO_FRAME, R.style.AppDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pcm_activity_fragment_container, null);
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_UP) {
                if (!onBackPressed()) {
                    dismiss();
                }
            }
            return true;
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gotoFragment(new GitRepoAddSSHWaysFragment());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.authorize);
    }

    private void gotoFragment(Fragment f) {
        FragmentTransaction it = getChildFragmentManager().beginTransaction()
                .add(R.id.frameLayout, f)
                .show(f);
        it.addToBackStack(UUID.randomUUID().toString());
        it.commitAllowingStateLoss();
    }

    @Override
    public boolean onBackPressed() {
        return FragmentHelper.onBackPressed(getChildFragmentManager());
    }

    @Override
    public void gotoGenerateNewKeys() {
        gotoFragment(new GitRepoAddSSHNewFragment());
    }

    @Override
    public void gotoProvideCustomKeys() {
        gotoFragment(new GitRepoAddSSHProvideFragment());
    }

    @Override
    public void onSubmitKeys(String publicKeyPath, String privateKeyPath) {
        if (callback != null) {
            callback.onInput(publicKeyPath, privateKeyPath);
        }
        callback = null;
        dismiss();
    }


    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (callback != null) {
            callback.onDismiss();
        }
    }

}
