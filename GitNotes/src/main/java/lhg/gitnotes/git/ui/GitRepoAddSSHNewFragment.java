package lhg.gitnotes.git.ui;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.BuildConfig;
import lhg.gitnotes.R;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;


import java.io.File;
import java.util.Date;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GitRepoAddSSHNewFragment extends AppBaseFragment {
    private TextView tvKey;
    private File pukFile, prkFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gitrepo_input_ssh_new, null);
    }

    private void testGenKeys2(File publicKey, File privateKey) {
        String pub = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDVETJOAxU14y3l1C5HxrJ+NyqYahj0zxTsb/8+GNao/OiPdpmKvkNwDRWKb2dy8GUIFmmOTYCCl3emSru7GzDh02Ro2sAjOY3+In7LU8YD0DYUrMmQPt6BTB+P8Tq0Q8IVn5qfUe9wQTWTbQy/ilBU85qpAMtXRYJ6jYXrOYEjGw== GitNotes20210731";
        String pri ="-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXQIBAAKBgQDVETJOAxU14y3l1C5HxrJ+NyqYahj0zxTsb/8+GNao/OiPdpmK\n" +
                "vkNwDRWKb2dy8GUIFmmOTYCCl3emSru7GzDh02Ro2sAjOY3+In7LU8YD0DYUrMmQ\n" +
                "Pt6BTB+P8Tq0Q8IVn5qfUe9wQTWTbQy/ilBU85qpAMtXRYJ6jYXrOYEjGwIDAQAB\n" +
                "AoGAHYXxe3/P25SsEnGRLB7rMrQAMfhZlupu0sg+DOOyMt5Ad0iOw/vTKl6VwoXn\n" +
                "RrTquvEoFTDGAtJsIN2wH6AH6LHgIPL0ZIg+oXzdeS8SVM7znN+jFPfzBoVLM3yw\n" +
                "D0MO2LiqGjmeQfrNX/bf7ocdUXjMjhUlMpcjDagp6idnTckCQQD455qdJ7ZEKyYM\n" +
                "RgCHkrrIbQuuSHBTfQk+izeztdelBL9qYqSKbtZHiHd+7r63mvuQTLMzzDeEMuBf\n" +
                "47KlvIqnAkEA2yQQ8AFA0nQFELKTIkCJzgK34eRd/ptjPUIBeO38h1aWd+ULrGW/\n" +
                "PeNFI1DngaoVpGyij98FsoyOcd4IY+pWbQJBAJe8GPVVEDfeVgOFaS08tcEZONW2\n" +
                "M0OyJcCK/hn/8MYYbthb6hK6Hsbc2nv27yzevhzppRUemXltynqqRG3k0mECQH1I\n" +
                "1VYhoUmwguTU80F13FMnQrGmugZCGC6Beg4FIYbOfh/1lwLs+LUNJg3Wx0ReVRk3\n" +
                "8oiGXN+DdDytT+avptECQQDdQVlNKHo2mYCE/qs1b4Tbnj8wvZRkenrIt8CbwLc9\n" +
                "kyO1Ryy33PrwytXGKlc0tJjqYJtSDaOY0YTlNZrLax+i\n" +
                "-----END RSA PRIVATE KEY-----";

        FileUtils.write(publicKey.getAbsolutePath(), pub);
        FileUtils.write(privateKey.getAbsolutePath(), pri);
    }


    private void genSSHRsa(String pukFile, String priFile, String comment) {
        JSch jsch = new JSch();
        try {
            KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            kpair.writePrivateKey(priFile);
            kpair.writePublicKey(pukFile, comment);
            System.out.println("Finger print: " + kpair.getFingerPrint());
            kpair.dispose();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pukFile = new File(getContext().getCacheDir(), System.currentTimeMillis() + ".publickey");
        prkFile = new File(getContext().getCacheDir(), System.currentTimeMillis() + ".privatekey");
        tvKey = findViewById(R.id.tvKey);
        findViewById(R.id.btnCopyKey).setOnClickListener(v -> copyKeys());
        findViewById(R.id.btnGenKey).setOnClickListener(v -> genKeys());
        findViewById(R.id.btnClone).setOnClickListener(v -> gotoClone());
        genKeys();
    }

    private void gotoClone() {
        GitRepoInputSshCallback callback = findCallback(GitRepoInputSshCallback.class);
        if (callback != null) {
            callback.onSubmitKeys(pukFile.getAbsolutePath(), prkFile.getAbsolutePath());
        }
    }

    private void copyKeys() {
        Utils.copy2Clipboard(getContext(), tvKey.getText().toString());
        ToastUtil.show(getContext(), getString(R.string.already_copy_the_key));
    }

    private void genKeys() {
        Single.fromCallable((Callable<CharSequence>) () -> {
            String pukDescription = "GitNotes" + DateFormat.format("yyyyMMdd", new Date());
            pukFile.delete();
            prkFile.delete();
            if (BuildConfig.DEBUG) {
                testGenKeys2(pukFile, prkFile);
            } else {
                genSSHRsa(pukFile.getAbsolutePath(), prkFile.getAbsolutePath(), pukDescription);
            }
            String publicKey = FileUtils.readFile(pukFile, "utf-8");
            String privateKey = FileUtils.readFile(prkFile, "utf-8");
            Log.i("KKKKKKK", publicKey);
            Log.i("KKKKKKK", privateKey);
            return publicKey;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    tvKey.setText(it);
                    copyKeys();
                }, throwable -> {
                    ToastUtil.show(getContext(), throwable.getMessage());
                })
        ;
    }

}
