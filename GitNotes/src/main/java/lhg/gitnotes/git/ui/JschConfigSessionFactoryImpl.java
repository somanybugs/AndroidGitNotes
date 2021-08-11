package lhg.gitnotes.git.ui;

import android.app.Activity;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import lhg.gitnotes.app.App;

import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class JschConfigSessionFactoryImpl extends org.eclipse.jgit.transport.JschConfigSessionFactory {

    private final FragmentManager fm;
    private String publicKeyPath;
    private String privateKeyPath;

    public JschConfigSessionFactoryImpl(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        Activity activity = App.instance().getTopActivity();
        if (activity instanceof FragmentActivity) {
            this.fm = ((FragmentActivity) activity).getSupportFragmentManager();
        } else {
            this.fm = null;
        }
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, com.jcraft.jsch.Session session) {
        session.setConfig("StrictHostKeyChecking","no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch sch = super.createDefaultJSch(fs);
        try {
            if (!TextUtils.isEmpty(privateKeyPath) && new File(privateKeyPath).exists()) {
                sch.addIdentity(privateKeyPath); //添加私钥文件
            } else if (fm != null) {
                String json = new UICredentialsProvider(fm).call("ssh");
                UICredentialsProvider.SSHKeyPath sshKeyPath = new Gson().fromJson(json, UICredentialsProvider.SSHKeyPath.class);
                sch.addIdentity(sshKeyPath.private_key_path); //添加私钥文件
                privateKeyPath = sshKeyPath.private_key_path;
                publicKeyPath = sshKeyPath.public_key_path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sch;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }
}