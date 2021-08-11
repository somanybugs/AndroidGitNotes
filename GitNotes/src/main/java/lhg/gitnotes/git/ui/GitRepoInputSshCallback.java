package lhg.gitnotes.git.ui;

public interface GitRepoInputSshCallback {
    void gotoGenerateNewKeys();
    void gotoProvideCustomKeys();
    void onSubmitKeys(String publicKeyPath, String privateKeyPath);
}
