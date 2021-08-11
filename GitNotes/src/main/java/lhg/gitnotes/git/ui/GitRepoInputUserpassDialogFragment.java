package lhg.gitnotes.git.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;
import lhg.gitnotes.R;
import lhg.common.OnBackPressedCallback;

import org.jetbrains.annotations.NotNull;


public class GitRepoInputUserpassDialogFragment extends AppCompatDialogFragment implements OnBackPressedCallback {
    TextInputLayout tilEmail, tilPassword;
    EditText editEmail, editPassword;
    DialogInputCallback callback;

    public GitRepoInputUserpassDialogFragment(DialogInputCallback callback) {
        this.callback = callback;
        setStyle(STYLE_NO_FRAME, R.style.AppDialogTheme);
    }

    @Override
    public int show(@NonNull @NotNull FragmentTransaction transaction, @Nullable @org.jetbrains.annotations.Nullable String tag) {
        return super.show(transaction, tag);
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public <T extends View> T findViewById(int id) {
        return getView().findViewById(id);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gitrepo_input_userpass, null);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        findViewById(R.id.btnOk).setOnClickListener(view1 -> {
           if (checkInput()) {
               submit(editEmail.getText().toString(), editPassword.getText().toString());
           }
        });

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.authorize);
    }


    private void submit(String email, String password) {
        if (callback != null) {
            callback.onInput(email, password);
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

    private boolean checkInput() {
        if (editEmail.length() == 0) {
            tilEmail.setError("please input username");
            return false;
        }
        if (editPassword.length() == 0) {
            tilPassword.setError("please input password");
            return false;
        }
       return true;
    }
}
