package lhg.gitnotes.git.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;
import lhg.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class GitRepoAddSSHProvideFragment extends AppBaseFragment {
    private static final int Request_load_prk_code = 111;
    private static final int Request_load_puk_code = 112;

    private TextInputLayout editPubKey, editPriKey;
    private File pukFile, prkFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gitrepo_input_ssh_provide, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pukFile = new File(getContext().getCacheDir(), System.currentTimeMillis() + ".publickey");
        prkFile = new File(getContext().getCacheDir(), System.currentTimeMillis() + ".privatekey");

        editPubKey = findViewById(R.id.editPubKey);
        editPriKey = findViewById(R.id.editPriKey);
        findViewById(R.id.btnLoadPuk).setOnClickListener(v -> loadPuk());
        findViewById(R.id.btnLoadPrk).setOnClickListener(v -> loadPrk());
        findViewById(R.id.btnClone).setOnClickListener(v -> gotoClone());
    }

    private void gotoClone() {
        GitRepoInputSshCallback callback = findCallback(GitRepoInputSshCallback.class);
        if (callback != null) {
            callback.onSubmitKeys(pukFile.getAbsolutePath(), prkFile.getAbsolutePath());
        }
    }

    private void loadPuk() {
        loadKeyFromFile(Request_load_puk_code);
    }

    private void loadPrk() {
        loadKeyFromFile(Request_load_prk_code);
    }

    private void loadKeyFromFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Request_load_prk_code && resultCode == Activity.RESULT_OK && data.getData() != null) {
            try (InputStream is = getContext().getContentResolver().openInputStream(data.getData())){
                String key = FileUtils.inputstream2text(is);
                editPriKey.getEditText().setText(key);
                prkFile.delete();
                FileUtils.write(prkFile.getAbsolutePath(), key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == Request_load_puk_code && resultCode == Activity.RESULT_OK && data.getData() != null) {
            try (InputStream is = getContext().getContentResolver().openInputStream(data.getData())){
                String key = FileUtils.inputstream2text(is);
                editPubKey.getEditText().setText(key);
                pukFile.delete();
                FileUtils.write(pukFile.getAbsolutePath(), key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
