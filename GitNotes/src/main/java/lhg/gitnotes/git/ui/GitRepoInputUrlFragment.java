package lhg.gitnotes.git.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;

import org.jetbrains.annotations.NotNull;

public class GitRepoInputUrlFragment extends AppBaseFragment {
    TextInputLayout tilUrl;
    EditText editUrl;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gitrepo_input_url, null);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tilUrl = findViewById(R.id.tilUrl);
        editUrl = findViewById(R.id.editUrl);

        findViewById(R.id.btnOk).setOnClickListener(view1 -> {
            if (editUrl.length() == 0) {
                tilUrl.setError("please input url");
                return;
            }
            String url = editUrl.getText().toString().trim();
            ((OnInputUrlCallback)getActivity()).onInputUrl(url);
        });
    }


    public interface OnInputUrlCallback {
        void onInputUrl(String url);
    }


}
