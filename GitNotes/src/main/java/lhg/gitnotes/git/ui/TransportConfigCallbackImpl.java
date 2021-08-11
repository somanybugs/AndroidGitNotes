package lhg.gitnotes.git.ui;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;

public class TransportConfigCallbackImpl implements TransportConfigCallback {
    public String privateKeyPath;
    JschConfigSessionFactoryImpl factory;

    public TransportConfigCallbackImpl(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    @Override
    public void configure(Transport transport) {
        factory = new JschConfigSessionFactoryImpl(privateKeyPath);
        if (transport instanceof TransportGitSsh) {
            ((TransportGitSsh) transport).setSshSessionFactory(factory);
        } else if (transport instanceof SshTransport) {
            ((SshTransport) transport).setSshSessionFactory(factory);
        }
    }

    public String getPrivateKeyPath() {
        return factory == null ? privateKeyPath : factory.getPrivateKeyPath();
    }

    public String getPublicKeyPath() {
        return factory == null ? null : factory.getPublicKeyPath();
    }

}
